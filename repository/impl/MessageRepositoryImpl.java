package com.cando.message.repository.impl;

import com.cando.core.common.Response;
import com.cando.core.common.Status;
import com.cando.core.exceptions.CDException;
import com.cando.core.exceptions.ExceptionFactory;
import com.cando.core.repository.impl.AbstractCDCRUDOperationsExtension;
import com.cando.events.aspects.Loggable;
import com.cando.events.common.IConstants;
import com.cando.events.common.IIndexConstants;
import com.cando.events.dto.CDJSONResponse;
import com.cando.events.model.CDUser;
import com.cando.events.repository.EventRepository;
import com.cando.message.mapper.MessageMapper;
import com.cando.message.mapper.NotificationMapper;
import com.cando.message.model.CDMessage;
import com.cando.message.relation.CDRelationMessage;
import com.cando.message.repository.MessageRepository;
import com.cando.message.repository.MessageRepositoryExtension;
import com.cando.ordermanagement.model.EventOrder;
import com.cando.ordermanagement.repository.EventOrderRepository;
import com.cando.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Package Name: com.cando.message.repository.impl
 * Author: pradeep
 */
public class MessageRepositoryImpl extends AbstractCDCRUDOperationsExtension<CDMessage> implements MessageRepositoryExtension
{
	@Autowired
	MessageRepository messageRepository;
	@Autowired
	UserRepository userRepository;
	@Autowired
	EventOrderRepository eventOrderRepository;
	@Autowired
	EventRepository eventRepository;

	@Override
	public CDMessage projectTo( final String objectUUID )
	{
		return messageRepository.findByObjectUUID( objectUUID );
	}

	@Override
	public String[] getSearchableProperties( final IConstants.EnumSearchContext searchContext )
	{
		return new String[0];
	}

	@Override
	public String getIndexForEntity()
	{
		return IIndexConstants.TYPE_ALIAS_CDMessage;
	}

	@Override
	public void sendNotificationToEventOrganizer( final EventOrder eventOrder ) throws CDException
	{

		CDUser user = eventRepository.getEventOrganizerOfTheOrder( eventOrder.getOrderId() );

		if ( user != null )
		{
			CDMessage message = new CDMessage();
			//todo hard coded to be removed
			//String body = user.getDisplayName() + " has purchased " + eventOrder.getTotalTicketsQuantity() + " tickets " + "with order id" + eventOrder.getOrderId() + "payment type " + eventOrder.getType().name();
//			String body = "new order with id " + eventOrder.getOrderId();
			String body = userRepository.getAuthenticatedUser() + " has been perchased a ticket";
			List<CDUser> users = new ArrayList<>();
			users.add( user );
			message.setType( IConstants.EnumMessageType.INBOX );
			message.setBody( body );
			message.setToUsers( users );
			messageRepository.createAndSendMessage( message, IConstants.EnumMessage.NOTIFICATION );
		}
	}

	@Override
	@Transactional
	@Loggable
	public void sendOrderStatusMsg( final EventOrder eventOrder ) throws CDException
	{
		String orderId = eventOrder.getOrderId();
		CDUser toUser = eventOrderRepository.getUserWhoPlacedOrder( orderId );

		//todo: just send message only confirmed
		if ( eventOrder.getStatus() == IConstants.EnumReservationStatus.CONFIRMED )
		{
			CDUser fromUser = getAuthenticatedUser();
			if ( toUser != null && fromUser != null )
			{
				CDMessage message = new CDMessage();
				String body = "your order " + orderId + "status is " + eventOrder.getStatus();
				List<CDUser> users = new ArrayList<>();
				users.add( toUser );
				message.setType( IConstants.EnumMessageType.INBOX );
				message.setBody( body );
				message.setToUsers( users );
				messageRepository.createAndSendMessage( message, IConstants.EnumMessage.MESSAGE );
			}
		}

	}

	@Override
	@Transactional
	@Loggable
	public CDMessage createAndSendMessage( CDMessage message, IConstants.EnumMessage messageType ) throws CDException
	{
		rejectIfNullOrEmpty( message, CDMessage.class.getSimpleName() );
		Boolean isNotification = messageType == IConstants.EnumMessage.NOTIFICATION;
		CDUser user = isNotification ? getAuthenticatedUserNoEx() : getAuthenticatedUser();

		List<CDUser> toUsers = message.getToUsers();
		if ( !( toUsers != null && toUsers.size() > 0 ) )
		{
			ExceptionFactory.createAndThrowException( Status.MIN_USERS_REQUIRED_TO_SEND_MESSAGE );
		}

		Response createdMessageResp = messageRepository.create( message );
		CDMessage createdMessage = createdMessageResp.get( CDMessage.class );
		rejectIfNullOrEmpty( createdMessage, CDMessage.class.getSimpleName() );

		IConstants.EnumMessage type = IConstants.EnumMessage.NOTIFICATION;

		if ( !isNotification )
		{
			type = IConstants.EnumMessage.MESSAGE;
			CDRelationMessage fromMsgRel = setMessageRelationProperties( createdMessage, user, type, IConstants.EnumMessageStatus.NEW, IConstants.EnumMessageRelationType.FROM );
		}

		for ( CDUser toUser : toUsers )
		{
			CDRelationMessage toMsgRel = setMessageRelationProperties( createdMessage, toUser, type, IConstants.EnumMessageStatus.NEW, IConstants.EnumMessageRelationType.TO );
		}
		return createdMessage;
	}

	@Override
	@Transactional
	@Loggable
	public CDJSONResponse createAndSendMessage( String message, String subject, List<String> toUsersEmails, String fromUserEmail )
	{
		CDJSONResponse jsonRes = new CDJSONResponse();
		Map<String, Object> msgResData = new HashMap<>();
		List<String> userEmailsNotFound = new ArrayList<>();
		try
		{
			rejectIfNullOrEmpty( message );
			rejectIfNullOrEmpty( fromUserEmail );

			if ( !( toUsersEmails != null && toUsersEmails.size() > 0 ) )
			{
				ExceptionFactory.createAndThrowException( Status.MIN_USERS_REQUIRED_TO_SEND_MESSAGE );
			}

			CDMessage cdMessage = new CDMessage();
			if ( fromUserEmail != null )
			{
				CDUser user = userRepository.findByEmail( fromUserEmail );
				cdMessage.setFromUser( user );
			}

			List<CDUser> toUsers = new ArrayList<>();
			for ( String userEmail : toUsersEmails )
			{
				CDUser user = userRepository.findByEmail( userEmail );
				if ( user != null )
				{
					toUsers.add( user );
				}
				else
				{
					userEmailsNotFound.add( userEmail );
				}
			}
			if ( userEmailsNotFound.size() > 0 )
			{
				msgResData.put( "userEmailsNotFound", userEmailsNotFound );
			}
			if ( toUsers.size() > 0 )
			{
				cdMessage.setBody( message );
				cdMessage.setSubject( subject );
				cdMessage.setToUsers( toUsers );
				cdMessage.setType( IConstants.EnumMessageType.INBOX );
				CDMessage createdMessage = messageRepository.createAndSendMessage( cdMessage, IConstants.EnumMessage.MESSAGE );
				msgResData.put( "sentMessage", createdMessage );
			}

			jsonRes.setStatus( IConstants.EnumResponseStatus.OK );
			jsonRes.setData( msgResData );
		}
		catch ( CDException e )
		{
			jsonRes.setStatus( IConstants.EnumResponseStatus.ERROR );
			jsonRes.setMessage( e.getMessage() );
		}
		return jsonRes;
	}

	@Override
	@Transactional
	@Loggable
	public Response getTopMessages() throws CDException
	{
		Response response = new Response();
		CDUser user = messageRepository.getAuthenticatedUser();
		Page<MessageMapper> messageMapPage = messageRepository.getAllInboxMessages( user.getObjectUUID(), new PageRequest( 0, 3 ) );
		int count = messageRepository.getCountOfAllInboxMessages( user.getObjectUUID() );
		Map<String, Object> msgsMap = new HashMap<>();
		msgsMap.put( "allMsgCount", count );
		msgsMap.put( "messageMap", messageMapPage.getContent() );
		response.add( "msgsMap", msgsMap );
		return response;
	}

	@Override
	@Transactional
	@Loggable
	public Response getTopNotifications() throws CDException
	{
		Response response = new Response();
		CDUser user = messageRepository.getAuthenticatedUser();
		Page<NotificationMapper> messageMapPage = messageRepository.getAllInboxNotifications( user.getObjectUUID(), new PageRequest( 0, 3 ) );
		int count = messageRepository.getCountOfAllInboxNotifications( user.getObjectUUID() );
		Map<String, Object> msgsMap = new HashMap<>();
		msgsMap.put( "totalUnreadNotifCount", count );
		msgsMap.put( "notificationMap", messageMapPage.getContent() );
		response.add( "msgsMap", msgsMap );
		return response;
	}

	private CDRelationMessage setMessageRelationProperties( CDMessage message, CDUser user, IConstants.EnumMessage messageType, IConstants.EnumMessageStatus messageStatus, IConstants.EnumMessageRelationType messageRelationType ) throws CDException
	{
		CDRelationMessage msgRel = getOrCreateRelationshipBetween( message, user, CDRelationMessage.class, CDRelationMessage.MESSAGE_RELATION, true );
		msgRel.setMessageRelationType( messageRelationType );
		msgRel.setMessageType( messageType );
		msgRel.setStatus( messageStatus );
		msgRel.setCreationDate( System.currentTimeMillis() );
		CDRelationMessage updatedMsgRel = n4jOperations.save( msgRel );
		return updatedMsgRel;
	}

	@Override
	@Transactional
	@Loggable
	public Response getMessages( IConstants.EnumMessage enumMessage, IConstants.EnumMessageRelationType type, IConstants.EnumMessageStatus status, int indexFrom, int indexTo ) throws CDException
	{
		Response response = new Response();
		Map<String, Object> msgsMap = new HashMap<>();
		Page<MessageMapper> messageMappers = null;
		if ( enumMessage == IConstants.EnumMessage.MESSAGE )
		{
			if ( type == IConstants.EnumMessageRelationType.TO )
			{
				if ( status == IConstants.EnumMessageStatus.NEW )
				{
					messageMappers = messageRepository.getAllUnreadMessages( userRepository.getAuthenticatedUser().getObjectUUID(), new PageRequest( indexFrom, indexTo ) );
				}
				else if ( status == null )
				{
					messageMappers = messageRepository.getAllInboxMessages( userRepository.getAuthenticatedUser().getObjectUUID(), new PageRequest( indexFrom, indexTo ) );
				}
			}
			else if ( type == IConstants.EnumMessageRelationType.FROM )
			{
				if ( !( status == IConstants.EnumMessageStatus.ARCHIVED ) )
				{
					messageMappers = messageRepository.getAllSentMessages( userRepository.getAuthenticatedUser().getObjectUUID(), new PageRequest( indexFrom, indexTo ) );
					// todo to be refactored
					for ( MessageMapper mapper : messageMappers.getContent() )
					{
						List<CDUser> cdUsers = messageRepository.getMessageReceivers( mapper.getMessage().getObjectUUID() );
						if ( cdUsers.size() == 1 && cdUsers.get( 0 ).getProfile() != null )
						{
							CDUser user = cdUsers.get( 0 );
							mapper.setProfile( user.getProfile().toString() );
							mapper.setPhotoUrl( user.getProfile().getPhoto() == null ? null : user.getProfile().getPhoto() );
							mapper.getMessage().setToUsers( cdUsers );
							mapper.setDisplayName( user.getDisplayName() );
						}
						else
						{
							mapper.setProfile( null );
							mapper.getMessage().setToUsers( cdUsers );
							String displayName = "";
							for ( CDUser user : cdUsers )
							{
								displayName = user.getDisplayName() + "," + displayName;
							}
							mapper.setDisplayName( displayName );
						}
					}
				}
			}
			msgsMap.put( "messageMap", messageMappers.getContent() );
			response.add( "msgsMap", msgsMap );
		}
		//here  not using relation type, status
		else if ( enumMessage == IConstants.EnumMessage.NOTIFICATION )
		{
			Page<NotificationMapper> messageList = messageRepository.getAllInboxNotifications( userRepository.getAuthenticatedUser().getObjectUUID(), new PageRequest( indexFrom, indexTo ) );
			msgsMap.put( "messageMap", messageList.getContent() );
			response.add( "msgsMap", msgsMap );
		}

		return response;
	}

	@Override
	@Transactional
	@Loggable
	public int getMessagesCountOf( IConstants.EnumMessage enumMessage, IConstants.EnumMessageRelationType type, IConstants.EnumMessageStatus status ) throws CDException
	{
		int count = 0;
		CDUser authUser = userRepository.getAuthenticatedUser();
		if ( enumMessage == IConstants.EnumMessage.MESSAGE )
		{
			if ( type == IConstants.EnumMessageRelationType.FROM )
			{
				if ( status == IConstants.EnumMessageStatus.NEW || status == null )
				{
					count = messageRepository.getCountOfAllSentMessages( authUser.getObjectUUID() );
				}
			}
			else if ( type == IConstants.EnumMessageRelationType.TO )
			{
				if ( status == IConstants.EnumMessageStatus.NEW )
				{
					count = messageRepository.getNewMessagesCount( authUser.getObjectUUID() );
				}
				else if ( status == null )
				{
					count = messageRepository.getCountOfAllInboxMessages( authUser.getObjectUUID() );
				}
			}
		}
		else if ( enumMessage == IConstants.EnumMessage.NOTIFICATION )
		{
			if ( status == IConstants.EnumMessageStatus.NEW )
			{
				count = messageRepository.getCountOfAllInboxUnreadNotifications( authUser.getObjectUUID() );
			}
			else if ( status == null )
			{
				count = messageRepository.getCountOfAllInboxNotifications( authUser.getObjectUUID() );
			}
		}
		return count;
	}

	@Override
	@Transactional
	@Loggable
	public Response changeStatusOfMessage( List<String> messageList, IConstants.EnumMessageRelationType type, IConstants.EnumMessage messageType, IConstants.EnumMessageStatus statusToSet, boolean isUpdate ) throws CDException
	{
		Response response = new Response();
		CDUser user = userRepository.getAuthenticatedUser();
		if ( messageList.size() > 0 )
		{
			for ( String messageUUID : messageList )
			{
				CDMessage message = messageRepository.projectTo( messageUUID );
				try
				{
					/*setMessageRelationProperties( message, user, messageType, statusToSet, type, isUpdate );*/
					updateMsgRelationsipProperties( messageUUID, messageType, type, statusToSet );
				}
				catch ( CDException e )
				{
					response.add( "status", false );
				}
				response.add( "status", true );
			}
		}
		else
		{
			response.add( "status", false );
		}
		return response;
	}

	/**
	 * this method is used to update the relation properties between user and the message
	 *
	 * @param messageId
	 * 		uuid of a message
	 * @param messageType
	 * 		type of the message
	 * @param relType
	 * 		type of the relation
	 * @param statusToSet
	 * 		status to set on relation
	 * @return CDRelationMessage
	 * @throws CDException
	 */
	public CDRelationMessage updateMsgRelationsipProperties( String messageId, IConstants.EnumMessage messageType, IConstants.EnumMessageRelationType relType, IConstants.EnumMessageStatus statusToSet ) throws CDException
	{
		CDUser user = getAuthenticatedUser();
		CDRelationMessage msgRel = messageRepository.getMessageRelation( user.getObjectUUID(), messageId, messageType, relType );
		CDRelationMessage updatedMsgRel = null;
		if ( msgRel != null )
		{
			if ( msgRel.getStatus() == statusToSet )
			{
				return msgRel;
			}
			msgRel.setStatus( statusToSet );
			String msgChanges = "Relation last changed on" + System.currentTimeMillis() + " by " + user.getObjectUUID();
			msgRel.addChangeHistory( msgChanges );
			updatedMsgRel = n4jOperations.save( msgRel );
		}
		return updatedMsgRel;

	}
}

package com.cando.message.controller;

import com.cando.core.common.Response;
import com.cando.core.exceptions.CDException;
import com.cando.core.exceptions.ExceptionFactory;
import com.cando.core.exceptions.UnAuthenticatedException;
import com.cando.events.common.IConstants;
import com.cando.events.dto.CDJSONResponse;
import com.cando.events.model.CDUser;
import com.cando.message.model.CDMessage;
import com.cando.message.repository.MessageRepository;
import com.cando.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Package Name: com.cando.message.controller
 * Author: pradeep
 */
@Controller
@RequestMapping( value = "/message" )
public class MessageController
{
	@Autowired
	MessageRepository messageRepository;
	@Autowired
	UserRepository userRepository;

	/**
	 * This method handles the requests for the request mapping "/message/composeMessageForm" (with request method being GET),
	 * and this controller is used  to redirect to compose box
	 *
	 * @param model
	 * 		Model object that contains the details which can be passed to the view layer
	 * @return view name of the compose box
	 * @throws CDException
	 */
	@RequestMapping( value = "/composeMessage", method = RequestMethod.GET )
	public String composeMessageForm( Model model ) throws CDException
	{
		String viewName = "inbox/compose";
		try
		{
			userRepository.getAuthenticatedUser();
			model.addAttribute( "message", new CDMessage() );
		}
		catch ( UnAuthenticatedException unae )
		{
			viewName = "redirectUrl:/auth/login";
		}
		catch ( CDException e )
		{
			viewName = "/user";
		}
		return viewName;
	}

	/**
	 * This method handles the requests for the request mapping "/message/composeMessage" (with request method being POST),
	 * and this method retrieves the message object from compose box
	 *
	 * @param message
	 * 		new message
	 * @param emailIds
	 * 		emailIds of users to send messgage
	 * @param model
	 * 		Model object that contains the details which can be passed to the view layer
	 * @return messagebox
	 */
	@RequestMapping( value = "/composeMessage", method = RequestMethod.POST )
	public String composeMessage( @ModelAttribute CDMessage message, String emailIds, RedirectAttributes model )
	{
		String viewName = "redirect:/message/inbox";
		try
		{
			CDUser user = userRepository.getAuthenticatedUser();

			if ( emailIds != null && message != null )
			{
				messageRepository.createAndSendMessage( message.getBody(), message.getSubject(), Arrays.asList( emailIds.split( "," ) ), user.getEmail() );
			}
			else
			{
				ExceptionFactory.createAndThrowException( "Invalid input" );
			}
		}
		catch ( UnAuthenticatedException unae )
		{
			viewName = "redirectUrl:/auth/login";
		}
		catch ( CDException e )
		{
			model.addFlashAttribute( "error", e.getMessage() );
		}
		return viewName;
	}

	/**
	 * This method handles the requests for the request mapping "/message/inbox" (with request method being GET),
	 * and this method is used to open the inbox
	 *
	 * @param model
	 * 		Model object that contains the details which can be passed to the view layer
	 * @return messagebox
	 */
	@RequestMapping( value = "/inbox" )
	public String inbox( Model model )
	{
		String viewName = "/inbox/messagebox";
		try
		{
			userRepository.getAuthenticatedUser();
			int countOfAllInboxMessages = messageRepository.getCountOfAllInboxMessages( userRepository.getAuthenticatedUser().getObjectUUID() );
			int countOfAllSentMessages = messageRepository.getCountOfAllSentMessages( userRepository.getAuthenticatedUser().getObjectUUID() );
			int pageLimit = 5;
			int totalPages = calculateTotalNoPages( countOfAllInboxMessages, pageLimit );
			if ( totalPages > 0 )
			{
				Response response = messageRepository.getMessages( IConstants.EnumMessage.MESSAGE, IConstants.EnumMessageRelationType.TO, null, 0, pageLimit );
				Object msgsMap = response.get( "msgsMap" );
				if ( msgsMap instanceof Map )
				{
					Map<String, Object> details = (Map<String, Object>) msgsMap;
					for ( Map.Entry<String, Object> entry : details.entrySet() )
					{
						model.addAttribute( entry.getKey(), entry.getValue() );
					}
				}
			}
			else
			{
				model.addAttribute( "messageMap", null );
			}
			model.addAttribute( "messageType", IConstants.EnumMessage.MESSAGE );
			model.addAttribute( "relationType", IConstants.EnumMessageRelationType.TO );
			model.addAttribute( "status", null );
			model.addAttribute( "mappingUrl", "/message/getMessages" );
			model.addAttribute( "totalPages", totalPages );
			model.addAttribute( "pn", 0 );
			model.addAttribute( "totalsentMessages", countOfAllSentMessages );
			model.addAttribute( "totalMessages", countOfAllInboxMessages );
		}
		catch ( UnAuthenticatedException unae )
		{
			viewName = "redirectUrl:/auth/login";
		}
		catch ( CDException e )
		{
			viewName = "/user";
		}
		return viewName;
	}

	/**
	 * This method handles the requests for the request mapping "/message/get/topmessages" (with request method being GET),
	 * and this controller is used  to get the top 3 messages
	 *
	 * @param model
	 * 		Model object that contains the details which can be passed to the view layer
	 * @param type
	 * 		type of messages to be get
	 * @return messages of a particular message type
	 */
	@RequestMapping( value = "/get/topmessages", method = RequestMethod.GET )
	public String getTopMessages( Model model, @RequestParam( value = "type", required = true ) IConstants.EnumMessage type )
	{
		String view = "/inbox/notifications2";
		try
		{
			Response response = ( type == IConstants.EnumMessage.NOTIFICATION ) ? messageRepository.getTopNotifications() : ( type == IConstants.EnumMessage.MESSAGE ) ? messageRepository.getTopMessages() : new Response();
			Object msgsMap = response.get( "msgsMap" );
			if ( msgsMap instanceof Map )
			{
				Map<String, Object> details = (Map<String, Object>) msgsMap;
				for ( Map.Entry<String, Object> entry : details.entrySet() )
				{
					model.addAttribute( entry.getKey(), entry.getValue() );
				}
			}
		}
		catch ( Exception e )
		{
			e.printStackTrace();
		}
		return view;
	}

	/**
	 * This method handles the requests for the request mapping "/message/unreadcount" (with request method being GET),
	 * and this controller is used to get the count unread messages
	 *
	 * @return count of unread messages
	 */
	@RequestMapping( value = "/unreadcount", method = RequestMethod.GET )
	public
	@ResponseBody
	CDJSONResponse getUnreadMessagesCount()
	{
		CDJSONResponse response = new CDJSONResponse();
		try
		{
			CDUser user = messageRepository.getAuthenticatedUser();
			int newMsgCount = messageRepository.getNewMessagesCount( user.getObjectUUID() );
			int newNotifCount = messageRepository.getCountOfAllInboxUnreadNotifications( user.getObjectUUID() );
			Map<String, Integer> unreadMsgCount = new HashMap<>();
			unreadMsgCount.put( "newMsgCount", newMsgCount );
			unreadMsgCount.put( "newNotificationsCount", newNotifCount );
			response.setData( unreadMsgCount );
			response.setStatus( IConstants.EnumResponseStatus.OK );
		}
		catch ( UnAuthenticatedException une )
		{
			response.status = IConstants.EnumResponseStatus.REDIRECT;
			response.redirectUrl = "/auth/login";
		}
		catch ( Exception e )
		{
			response.status = IConstants.EnumResponseStatus.ERROR;
			response.message = e.getMessage();
		}
		return response;
	}

	/**
	 * This method handles the requests for the request mapping "/message/getMessagesCountOf" (with request method being GET),
	 * and this controller get the count of particular messages
	 *
	 * @param enumMessage
	 * 		enumMessage is a message type
	 * @param type
	 * 		type is a Relation type
	 * @param status
	 * 		status of a relation
	 * @return count of messages
	 */
	@RequestMapping( value = "/getMessagesCountOf" )
	@ResponseBody
	public CDJSONResponse getMessagesCountOf( @RequestParam( value = "enumMessage" ) IConstants.EnumMessage enumMessage, @RequestParam( value = "type" ) IConstants.EnumMessageRelationType type, @RequestParam( value = "status", required = false ) IConstants.EnumMessageStatus status )
	{
		CDJSONResponse response = new CDJSONResponse();
		try
		{
			userRepository.getAuthenticatedUser();
			int count = messageRepository.getMessagesCountOf( enumMessage, type, status );
			response.setData( count );
			response.setStatus( IConstants.EnumResponseStatus.OK );
		}
		catch ( UnAuthenticatedException une )
		{
			response.status = IConstants.EnumResponseStatus.REDIRECT;
			response.message = une.getMessage();
			response.redirectUrl = "/auth/login";
		}
		catch ( Exception e )
		{
			response.status = IConstants.EnumResponseStatus.ERROR;
			response.message = e.getMessage();
			response.redirectUrl = "/user";
		}

		return response;
	}

	/**
	 * This method handles the requests for the request mapping "/message/getMessages" (with request method being GET),
	 * and this method redirects the view for displaying message details
	 *
	 * @param enumMessage
	 * 		enumMessage is a message type
	 * @param type
	 * 		type is a Relation type
	 * @param status
	 * 		status of a relation
	 * @param pageNo
	 * 		messages in pageNo
	 * @param model
	 * 		Model object that contains the details which can be passed to the view layer
	 * @return view that loads the messages  in page
	 * @throws CDException
	 */
	@RequestMapping( value = "/getMessages" )
	public String getMessages( @RequestParam( value = "enumMessage" ) IConstants.EnumMessage enumMessage, @RequestParam( value = "type" ) IConstants.EnumMessageRelationType type, @RequestParam( value = "status", required = false ) IConstants.EnumMessageStatus status, @RequestParam( value = "pageNo", required = false ) Integer pageNo, Model model ) throws CDException
	{
		String viewName = "inbox/message";
		int pageNumber = pageNo == null ? 0 : pageNo.intValue();
		int pageLimit = 5;
		int countOfMessages = messageRepository.getMessagesCountOf( enumMessage, type, status );
		int totalPages = calculateTotalNoPages( countOfMessages, pageLimit );
		pageNumber = ( pageNumber < 0 ) ? 0 : ( pageNumber >= totalPages ) ? totalPages - 1 : pageNo;
		if ( totalPages > 0 )
		{
			Response response = messageRepository.getMessages( enumMessage, type, status, pageNumber, pageLimit );
			Object msgsMap = response.get( "msgsMap" );
			if ( msgsMap instanceof Map )
			{
				Map<String, Object> details = (Map<String, Object>) msgsMap;
				for ( Map.Entry<String, Object> entry : details.entrySet() )
				{
					model.addAttribute( entry.getKey(), entry.getValue() );
				}
			}
		}
		else
		{
			model.addAttribute( "messageMap", null );
		}
		model.addAttribute( "totalMessages", countOfMessages );
		model.addAttribute( "messageType", enumMessage );
		model.addAttribute( "relationType", type );
		model.addAttribute( "status", status );
		model.addAttribute( "mappingUrl", "/message/getMessages" );
		model.addAttribute( "totalPages", totalPages );
		model.addAttribute( "pn", pageNumber );
		return viewName;
	}

	/**
	 * This method handles the requests for the request mapping "/message/showMessage" (with request method being GET),
	 * and this method redirects the view for displaying message details
	 *
	 * @param messageType
	 * 		type of a message
	 * @param relationType
	 * 		type of relation
	 * @param relationStatus
	 * 		status of a message
	 * @param messageUUID
	 * 		objectUUID of a message
	 * @param model
	 * 		Model object that contains the details which can be passed to the view layer
	 * @return count of messages
	 * @throws CDException
	 */
	@RequestMapping( value = "/showMessage" )
	public String showMessage( @RequestParam( value = "messageType" ) IConstants.EnumMessage messageType, @RequestParam( value = "relationType" ) IConstants.EnumMessageRelationType relationType, @RequestParam( value = "relationStatus" ) IConstants.EnumMessageStatus relationStatus, @RequestParam( value = "messageUUID" ) String messageUUID, @RequestParam( value = "photo" ) String photo, @RequestParam( value = "displayName" ) String displayName, Model model ) throws CDException
	{
		String viewName = "inbox/message2";
		try
		{
			List<String> messageIds = new ArrayList<>( 0 );
			messageIds.add( messageUUID );
			userRepository.getAuthenticatedUser();
			if ( relationType == IConstants.EnumMessageRelationType.TO )
			{
				if ( relationStatus == IConstants.EnumMessageStatus.NEW )
				{
					messageRepository.changeStatusOfMessage( messageIds, relationType, messageType, IConstants.EnumMessageStatus.READ, true );
				}
			}
			CDMessage message = messageRepository.projectAndRejectIfNullOrEmpty( messageUUID, CDMessage.class );
			model.addAttribute( "body", message.getBody() );
			model.addAttribute( "subject", message.getSubject() );

			model.addAttribute( "photo", photo );
			model.addAttribute( "displayName", displayName );
			model.addAttribute( "messageUUID", messageUUID );
		/*	model.addAttribute( "body", body );
			model.addAttribute( "subject", subject );*/
			model.addAttribute( "messageType", messageType );
			model.addAttribute( "relationType", relationType );
			model.addAttribute( "relationStatus", relationStatus );
		}
		catch ( CDException e )
		{
			viewName = "redirectUrl:/auth/login";
		}
		return viewName;
	}


	/**
	 * This method handles the requests for the request mapping "/message/markMessageAs" (with request method being POST),
	 * and this method marks messages as status to set
	 *
	 * @param type
	 * 		type is a Relation type
	 * @param messageType
	 * 		messageType for which status has to be change
	 * @param statusToSet
	 * 		status to be change on relation
	 * @return CDJSONResponse
	 * @throws CDException
	 */
	@RequestMapping( value = "/markMessageAs", method = RequestMethod.GET )
	public String markMessageAs( @RequestParam( value = "messageType" ) IConstants.EnumMessage messageType, @RequestParam( value = "type" ) IConstants.EnumMessageRelationType type, @RequestParam( value = "list" ) List<String> messageUUID, @RequestParam( value = "statusToSet" ) IConstants.EnumMessageStatus statusToSet, Model model ) throws CDException
	{
		String viewName = "/inbox/messagebox";
		try
		{
			userRepository.getAuthenticatedUser();
			messageRepository.changeStatusOfMessage( messageUUID, type, messageType, statusToSet, true );
			viewName = "redirect:/message/inbox";
		}
		catch ( UnAuthenticatedException nae )
		{
			viewName = "redirect:/auth/login";
		}
		catch ( CDException e )
		{
			e.printStackTrace();
		}
		return viewName;
	}

	/**
	 * this method is used to calculate the total no of pages, which are fit for total no of messages
	 *
	 * @param messagesCount
	 * 		count of messages
	 * @param pageLimit
	 * 		no of messages per page
	 * @return count no of pages
	 */
	private int calculateTotalNoPages( int messagesCount, int pageLimit )
	{
		int numPages = messagesCount / pageLimit;
		return ( messagesCount % pageLimit == 0 ? numPages : numPages + 1 );
	}
}


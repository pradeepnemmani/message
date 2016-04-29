package com.cando.message.repository;

import com.cando.core.common.Response;
import com.cando.core.exceptions.CDException;
import com.cando.core.repository.CDCRUDOperationsExtension;
import com.cando.events.common.IConstants;
import com.cando.events.dto.CDJSONResponse;
import com.cando.message.model.CDMessage;
import com.cando.ordermanagement.model.EventOrder;

import java.util.List;

/**
 * Package Name: com.cando.message.repository
 * Author: Pradeep kumar
 */
public interface MessageRepositoryExtension extends CDCRUDOperationsExtension<CDMessage>
{
	/**
	 * This method is used to  send notification message to event organizer that user has purchased tickets
	 *
	 * @param eventOrder
	 * 		eventOrder order belonging to event
	 * @throws CDException
	 */
	public void sendNotificationToEventOrganizer( EventOrder eventOrder ) throws CDException;

	/**
	 * This method is used to  send  message to user has purchased tickets when order is approved
	 *
	 * @param eventOrder
	 * 		eventOrder order belonging to event message to be sent
	 * @throws CDException
	 */
	public void sendOrderStatusMsg( EventOrder eventOrder ) throws CDException;


	/**
	 * This method is used to create and send message
	 *
	 * @param message
	 * 		message object the message to be sent
	 * @param messageType
	 * 		type of message notification/message
	 * @return new created message
	 * @throws CDException
	 * 		if any exception is thrown
	 */

	public CDMessage createAndSendMessage( CDMessage message, IConstants.EnumMessage messageType ) throws CDException;

	/**
	 * This method is used to create and send message to localUsers in within the portal
	 *
	 * @param message
	 * 		message to be sent
	 * @param toUsersEmails
	 * 		list of toUsersEmails to send message
	 * @param fromUserEmail
	 * 		email id of the user form whom the message to be sent
	 * @return Status of the message as json response
	 */
	public CDJSONResponse createAndSendMessage( String message, String subject, List<String> toUsersEmails, String fromUserEmail );

	/**
	 * This method is used to get the top 3 messages of the user
	 *
	 * @return Response object total messages count and top 3 messages
	 * @throws CDException
	 */
	public Response getTopMessages() throws CDException;

	/**
	 * This method is used to get the top 3 notifications of the user
	 *
	 * @return Response object total notifications count and top 3 notifications
	 * @throws CDException
	 */

	public Response getTopNotifications() throws CDException;

	/**
	 * this method is used to messages
	 *
	 * @param enumMessage
	 * 		enumMessage type of messages
	 * @param type
	 * 		relation type between user and message
	 * @param status
	 * 		status of relation
	 * @param indexFrom
	 * 		messages fromIndex
	 * @param indexTo
	 * 		messages toIndex
	 * @return messages
	 * @throws CDException
	 */
	public Response getMessages( IConstants.EnumMessage enumMessage, IConstants.EnumMessageRelationType type, IConstants.EnumMessageStatus status, int indexFrom, int indexTo ) throws CDException;

	/**
	 * this method retrieve the count of messages
	 *
	 * @param enumMessage
	 * 		enumMessage type of messages
	 * @param type
	 * 		relation type between user and message
	 * @param status
	 * 		status of relation
	 * @return count of messages
	 * @throws CDException
	 */
	public int getMessagesCountOf( IConstants.EnumMessage enumMessage, IConstants.EnumMessageRelationType type, IConstants.EnumMessageStatus status ) throws CDException;

	/**
	 * this method is used to change the status of messages
	 *
	 * @param list
	 * 		list of messageUUID to which relation status has to change
	 * @param type
	 * 		relation type between user and message
	 * @param messageType
	 * 		status to set on message type
	 * @param statusToSet
	 * 		statusToSet on relation
	 * @param isUpdate
	 * 		update to be perform ornot
	 * @return response
	 * @throws CDException
	 */
	public Response changeStatusOfMessage( List<String> list, IConstants.EnumMessageRelationType type, IConstants.EnumMessage messageType, IConstants.EnumMessageStatus statusToSet, boolean isUpdate ) throws CDException;
}

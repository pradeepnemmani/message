package com.cando.message.repository;

import com.cando.core.repository.CDGraphRepository;
import com.cando.events.common.IConstants;
import com.cando.events.model.CDUser;
import com.cando.message.mapper.MessageMapper;
import com.cando.message.mapper.NotificationMapper;
import com.cando.message.model.CDMessage;
import com.cando.message.relation.CDRelationMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Package Name: com.cando.message.repository
 * Author: Pradeep Kumar
 */
public interface MessageRepository extends CDGraphRepository<CDMessage>, MessageRepositoryExtension
{
	/*// -------------------------------------------------------------------------
	// Derived finder methods
	//
	// -------------------------------------------------------------------------

	// Derived Query methods.
	// Please see META-INF/neo4j-named-queries.properties for the queries
	// -------------------------------------------------------------------------
	*/

	/**
	 * this method is used to get the count of new messages count
	 *
	 * @param userUUID
	 * 		objectUUID of a user
	 * @return count of new messages
	 */
	int getNewMessagesCount( @Param( "userUUID" ) String userUUID );

	/**
	 * this method is used to retrieve inbox messages
	 *
	 * @param userUUID
	 * 		objectUUID of a user
	 * @param pageable
	 * 		pageble request
	 * @return inbox messages
	 */
	Page<MessageMapper> getAllInboxMessages( @Param( "userUUID" ) String userUUID, Pageable pageable );

	/**
	 * this method is used to retrieve the notifications
	 *
	 * @param userUUID
	 * 		objectUUID of a user
	 * @param pageable
	 * 		pageble request
	 * @return notifications
	 */
	Page<NotificationMapper> getAllInboxNotifications( @Param( "userUUID" ) String userUUID, Pageable pageable );

	/**
	 * this method is used to get count of inbox messages
	 *
	 * @param userUUID
	 * 		objectUUID of a user
	 * @return count of all inbox messages
	 */
	int getCountOfAllInboxMessages( @Param( "userUUID" ) String userUUID );

	/**
	 * this method is used to get count notifications
	 *
	 * @param userUUID
	 * 		objectUUID of a user
	 * @return count of all notifications
	 */
	int getCountOfAllInboxNotifications( @Param( "userUUID" ) String userUUID );

	/**
	 * this method is used to retrieve all sent messages
	 *
	 * @param userUUID
	 * 		objectUUID of a user
	 * @param pageable
	 * 		pageble request
	 * @return sent mesages
	 */
	Page<MessageMapper> getAllSentMessages( @Param( "userUUID" ) String userUUID, Pageable pageable );

	/**
	 * this method is used to get the list of receivers of message
	 *
	 * @param messageUUID
	 * 		objectUUID of message
	 * @return
	 */
	List<CDUser> getMessageReceivers( @Param( "messageUUID" ) String messageUUID );

	/**
	 * this method is used to  retrieve count of all sent messages
	 *
	 * @param userUUID
	 * 		objectUUID of a user
	 * @return count of sent messages
	 */
	int getCountOfAllSentMessages( @Param( "userUUID" ) String userUUID );

	/**
	 * this method is used to retrieve all unread messages
	 *
	 * @param userUUID
	 * 		objectUUID of a user
	 * @param pageable
	 * 		pageble request
	 * @return unread messages
	 */
	Page<MessageMapper> getAllUnreadMessages( @Param( "userUUID" ) String userUUID, Pageable pageable );

	/**
	 * this method is used to retrieve the count of unread Notificationsr
	 *
	 * @param userUUID
	 * 		objectUUID of a user
	 * @return messages
	 */
	int getCountOfAllInboxUnreadNotifications( @Param( "userUUID" ) String userUUID );

	/**
	 * This method is used to get message relation between user and message
	 *
	 * @param userUUID
	 * 		uuid of the user
	 * @param messageUUID
	 * 		uuid of the message
	 * @param messageType
	 * 		type of the message
	 * @param messageRelType
	 * 		relation type of the message
	 * @return
	 */
	CDRelationMessage getMessageRelation( @Param( "userUUID" ) String userUUID, @Param( "messageUUID" ) String messageUUID, @Param( "messageType" ) IConstants.EnumMessage messageType, @Param( "messageRelType" ) IConstants.EnumMessageRelationType messageRelType );
}

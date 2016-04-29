package com.cando.message.relation;

import com.cando.events.common.IConstants;
import com.cando.events.model.CDUser;
import com.cando.events.relation.AbstractCDRelation;
import com.cando.message.model.CDMessage;
import org.springframework.data.neo4j.annotation.RelationshipEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * Package Name: com.cando.message.relation
 * Author: pradeep
 */
@RelationshipEntity(type = CDRelationMessage.MESSAGE_RELATION)
public class CDRelationMessage extends AbstractCDRelation<CDMessage, CDUser>
{
	public static final String MESSAGE_RELATION = "MESSAGE_RELATION";
	private IConstants.EnumMessageRelationType messageRelationType;
	private IConstants.EnumMessage messageType;
	private IConstants.EnumMessageStatus status;
	private List<String> changeHistory;
	private long creationDate;

	@Override
	public String getRelationType()
	{
		return CDRelationMessage.MESSAGE_RELATION;
	}

	/**
	 * @return the relationType with another node
	 */
	public IConstants.EnumMessageRelationType getMessageRelationType()
	{
		return messageRelationType;
	}

	public void setMessageRelationType( final IConstants.EnumMessageRelationType messageRelationType )
	{
		this.messageRelationType = messageRelationType;
	}

	/**
	 * It is used to return type of the message
	 *
	 * @returnype message type
	 */
	public IConstants.EnumMessage getMessageType()
	{
		return messageType;
	}

	/**
	 * It is used to set type of the message
	 *
	 * @param messageType
	 * 		type of the message
	 */
	public void setMessageType( final IConstants.EnumMessage messageType )
	{
		this.messageType = messageType;
	}

	/**
	 * It returns date of message created
	 *
	 * @return message creation date
	 */
	public long getCreationDate()
	{
		return creationDate;
	}

	/**
	 * It is used to set creation date
	 *
	 * @param creationDate
	 * 		message creation date
	 */
	public void setCreationDate( final long creationDate )
	{
		this.creationDate = creationDate;
	}

	/**
	 * It returns status of the message
	 *
	 * @return message status
	 */
	public IConstants.EnumMessageStatus getStatus()
	{
		return status;
	}

	/**
	 * Set status of the message
	 *
	 * @param status
	 * 		message status
	 */
	public void setStatus( final IConstants.EnumMessageStatus status )
	{
		this.status = status;
	}

	/**
	 * It is used set change history of the order
	 *
	 * @return list of strings with changed history
	 */
	public List<String> getChangeHistory()
	{
		return changeHistory;
	}

	/**
	 * It is used to set change history when relation is changed this property gets updated with
	 * the details on the changes (e.x. Changed On <timestamp> by <user-uuid>: <list of properties that were changed>);
	 *
	 * @param changeHistory
	 */
	public void setChangeHistory( final List<String> changeHistory )
	{
		this.changeHistory = changeHistory;
	}

	public void addChangeHistory( String message )
	{
		if ( changeHistory == null )
		{
			changeHistory = new ArrayList<>();
			changeHistory.add( message );
		}
		else
		{
			changeHistory.add( message );
		}
	}
}
package com.cando.message.mapper;

import com.cando.message.model.CDMessage;
import com.cando.message.relation.CDRelationMessage;
import org.springframework.data.neo4j.annotation.QueryResult;
import org.springframework.data.neo4j.annotation.ResultColumn;

/**
 * Package Name: com.cando.message.mapper
 * Author: Pradeep
 */
@QueryResult
public class NotificationMapper
{
	@ResultColumn( "message" )
	CDMessage message;

	@ResultColumn( "relation" )
	CDRelationMessage relation;

	/**
	 * Returns the message object
	 *
	 * @return cdMessage object
	 */
	public CDMessage getMessage()
	{
		return message;
	}

	/**
	 * Set the message object
	 *
	 * @param message
	 */
	public void setMessage( final CDMessage message )
	{
		this.message = message;
	}

	/**
	 * Returns the relation between message and user
	 *
	 * @return messageRelation
	 */
	public CDRelationMessage getRelation()
	{
		return relation;
	}

	/**
	 * Sets the messageRelation
	 *
	 * @param relation
	 */
	public void setRelation( final CDRelationMessage relation )
	{
		this.relation = relation;
	}

}

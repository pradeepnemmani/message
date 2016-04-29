package com.cando.message.model;

import com.cando.core.exceptions.ExceptionFactory;
import com.cando.core.exceptions.InvalidDataException;
import com.cando.events.common.IConstants;
import com.cando.events.common.IIndexConstants;
import com.cando.events.model.AbstractCDEntity;
import com.cando.events.model.CDUser;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Package Name: com.cando.message.model
 * Author: Pradeep
 */
@NodeEntity
@TypeAlias( IIndexConstants.TYPE_ALIAS_CDMessage )
public class CDMessage extends AbstractCDEntity
{
	private String subject;
	private String body;
	private IConstants.EnumMessageType type;
	//private IConstants.EnumMessage type;
	private List<String> attachments;
	private transient CDUser fromUser;
	private transient List<CDUser> toUsers;

	/**
	 * It used to get subject of the message
	 *
	 * @return subject of the message
	 */
	public String getSubject()
	{
		return subject;
	}

	/**
	 * It used to set subject of the message
	 *
	 * @param subject
	 * 		message subject
	 */
	public void setSubject( final String subject )
	{
		this.subject = subject;
	}

	/**
	 * It used to get body of the message
	 *
	 * @return message body
	 */
	public String getBody()
	{
		return body;
	}

	/**
	 * It used to get body of the message
	 *
	 * @param body
	 * 		message body
	 */
	public void setBody( final String body )
	{
		this.body = body;
	}

	/**
	 * It used to get type of the message
	 *
	 * @return type of the message
	 */
	public IConstants.EnumMessageType getType()
	{
		return type;
	}

	/**
	 * It used to set type of the message
	 *
	 * @param type
	 */
	public void setType( final IConstants.EnumMessageType type )
	{
		this.type = type;
	}
/*

	*/
/**
 * It used to get type of the message
 *
 * @return message type
 *//*

	public IConstants.EnumMessage getType()
	{
		return type;
	}

	*/
/**
 * It used to set type of the message
 *
 * @return message type
 *//*

	public void setType( final IConstants.EnumMessage type )
	{
		this.type = type;
	}
*/

	/**
	 * It used to get list of attachments of the message
	 *
	 * @return message attachments
	 */
	public List<String> getAttachments()
	{
		return attachments;
	}

	/**
	 * It used to set list of attachments of the message
	 *
	 * @param attachments
	 * 		message attachments
	 */
	public void setAttachments( final List<String> attachments )
	{
		this.attachments = attachments;
	}

	/**
	 * It used to get user who set sent message
	 *
	 * @return user who set sent message
	 */
	public CDUser getFromUser()
	{
		return fromUser;
	}

	/**
	 * It used to set user who set sent message
	 *
	 * @param fromUser
	 * 		user who set sent message
	 */
	public void setFromUser( final CDUser fromUser )
	{
		this.fromUser = fromUser;
	}

	/**
	 * It used to get user list of used to whom message to be sent
	 *
	 * @return users list
	 */
	public List<CDUser> getToUsers()
	{
		return toUsers;
	}

	/**
	 * It used to set user list of used to whom message to be sent
	 *
	 * @param toUsers
	 * 		users list
	 */
	public void setToUsers( final List<CDUser> toUsers )
	{
		this.toUsers = toUsers;
	}


	public void addUser( CDUser user )
	{
		if ( toUsers == null )
		{
			toUsers = new ArrayList<>();
		}
		toUsers.add( user );
	}

	@Override
	public boolean isValidObject()
	{
		boolean verdict = super.isValidObject();
		if ( verdict )
		{
			verdict = StringUtils.hasLength( body );
		}
		return verdict;
	}

	@Override
	public void checkValidness( final boolean ignoreIds ) throws InvalidDataException
	{
		super.checkValidness( ignoreIds );
		Map<String, Object> invalidProps = new HashMap<>();
		if ( body == null )
		{
			invalidProps.put( FIELDS.MESSSAGE_BODY.fieldName(), body );
		}
		if ( type == null )
		{
			invalidProps.put( FIELDS.MESSAGE_TYPE.fieldName(), type );
		}
		if ( !invalidProps.isEmpty() )
		{
			ExceptionFactory.createAndThrowInvalidDataException( this, invalidProps );
		}

	}

	public static enum FIELDS
	{
		MESSSAGE_BODY( "body" ),
		MESSSAGE_SUBJECT( "subject" ),
		MESSAGE_TYPE( "type" ),
		MESSAGE_ATTACHMENTS( "attachments" );

		private final String fieldName;

		FIELDS( String fieldName )
		{
			this.fieldName = fieldName;
		}

		public String fieldName()
		{
			return fieldName;
		}
	}
}
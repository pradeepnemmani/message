package com.cando.message.mapper;

import com.cando.core.common.Purifier;
import com.cando.events.dto.user.Profile;
import com.cando.events.util.CDUtils;
import com.cando.message.model.CDMessage;
import com.cando.message.relation.CDRelationMessage;
import org.springframework.data.neo4j.annotation.QueryResult;
import org.springframework.data.neo4j.annotation.ResultColumn;
import org.springframework.util.StringUtils;

/**
 * Package Name: com.cando.message.mapper
 * Author: Pradeep
 */
@QueryResult
public class MessageMapper extends NotificationMapper
{
	@ResultColumn( "message" )
	CDMessage message;

	@ResultColumn( "relation" )
	CDRelationMessage relation;

	@ResultColumn( "userUUID" )
	String userUUID;

	@ResultColumn( "email" )
	String email;

	@ResultColumn( "firstName" )
	String firstName;

	@ResultColumn( "lastName" )
	String lastName;

	@ResultColumn( "profile" )
	String profile;

	String photoUrl;

	String displayName;

	/**
	 * //	 * This method returns the email of the user who is a group member
	 * //	 *
	 * //	 * @return email address
	 * //
	 */
	public String getEmail()
	{
		return email;
	}

	/**
	 * Sets the specified email address
	 *
	 * @param email
	 * 		email address to set
	 */
	public void setEmail( final String email )
	{
		this.email = Purifier.clean( email );
	}

	/**
	 * This method returns the first name of the user who is a group member
	 *
	 * @return first name
	 */
	public String getFirstName()
	{
		return firstName;
	}

	/**
	 * Sets the first name of the user
	 *
	 * @param firstName
	 * 		first name of the user
	 */
	public void setFirstName( final String firstName )
	{
		this.firstName = Purifier.clean( firstName );
		setName();
	}

	/**
	 * Returns the last name of the user who is a group member
	 *
	 * @return last name
	 */
	public String getLastName()
	{
		return lastName;
	}

	/**
	 * Sets the last name of the user
	 *
	 * @param lastName
	 * 		last name of the user
	 */
	public void setLastName( final String lastName )
	{
		this.lastName = Purifier.clean( lastName );
		setName();
	}

	/**
	 * Sets the photoUrl of the user
	 *
	 * @param photoUrl
	 * 		photo of the user
	 */
	public void setPhotoUrl( final String photoUrl )
	{
		this.photoUrl = photoUrl;
	}

	/**
	 * Returns the profile data of the user as a string
	 *
	 * @return profile information in string representation
	 */
	public String getProfile()
	{
		return profile;
	}

	/**
	 * Sets the specified profile
	 *
	 * @param profile
	 * 		profile data (in string format)
	 */
	public void setProfile( final String profile )
	{
		this.profile = Purifier.clean( profile );
		if ( !StringUtils.isEmpty( this.profile ) )
		{
			Profile p = CDUtils.convertJSONToJavaObject( this.profile, Profile.class );
			photoUrl = p != null ? p.getPhoto() : "";
		}
	}

	/**
	 * Returns the url to group member's photo
	 *
	 * @return string representing user's photo url or empty string if the user has not set it up
	 */
	public String getPhotoUrl()
	{
		return photoUrl;
	}

	/**
	 * It used to get from users uuid
	 *
	 * @return users uuid
	 */
	public String getUserUUID()
	{
		return userUUID;
	}

	/**
	 * It used to set from users uuid
	 *
	 * @param userUUID
	 */
	public void setUserUUID( final String userUUID )
	{
		this.userUUID = userUUID;
	}

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

	/**
	 * Sets the Dispalay Name
	 *
	 * @param displayName
	 * 		name of a user
	 */
	public void setDisplayName( final String displayName )
	{
		this.displayName = displayName;
	}

	/**
	 * Returns the from user displayName
	 *
	 * @return user name
	 */
	public String getDisplayName()
	{
		return displayName;
	}

	/**
	 * Set user Name
	 */
	public void setName()
	{
		String emailWithoutDomain = StringUtils.hasLength( email ) && email.indexOf( "@" ) > 0 ? email.substring( 0, email.indexOf( "@" ) ) : "";
		String name = StringUtils.hasLength( firstName ) ? firstName : "";
		name = Purifier.clean( name );
		name = StringUtils.hasLength( name ) ? name : StringUtils.hasLength( lastName ) ? lastName : "";
		name = Purifier.clean( name );
		name = StringUtils.hasLength( name ) ? name : emailWithoutDomain;
		this.displayName = name;
	}

}

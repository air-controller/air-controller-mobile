package com.youngfeng.android.assistant.util

import android.accounts.Account
import android.content.Context
import com.youngfeng.android.assistant.web.entity.ContactAccount
import com.youngfeng.android.assistant.web.entity.ContactBasicInfo
import com.youngfeng.android.assistant.web.entity.ContactDataType
import com.youngfeng.android.assistant.web.entity.ContactDetail
import com.youngfeng.android.assistant.web.entity.ContactFieldItem
import com.youngfeng.android.assistant.web.entity.ContactGroup
import com.youngfeng.android.assistant.web.entity.ContactNote
import com.youngfeng.android.assistant.web.response.ContactAccountInfo
import com.youngfeng.android.assistant.web.response.ContactAndGroups
import contacts.core.Contacts
import contacts.core.ContactsFields
import contacts.core.GroupsFields
import contacts.core.asc
import contacts.core.entities.AddressEntity
import contacts.core.entities.EmailEntity
import contacts.core.entities.ImEntity
import contacts.core.entities.PhoneEntity
import contacts.core.entities.RawContact
import contacts.core.entities.RelationEntity
import contacts.core.equalTo
import contacts.core.util.toRawContact

object ContactUtil {

    fun getAllContacts(context: Context): List<ContactBasicInfo> =
        Contacts(context).query().orderBy(ContactsFields.DisplayNamePrimary.asc()).find()
            .flatMap { it.rawContacts }
            .map { rawContact ->
                ContactBasicInfo(
                    id = rawContact.id,
                    contactId = rawContact.contactId,
                    displayNamePrimary = rawContact.name?.displayName,
                    phoneNumber = rawContact.phones.sortedByDescending { it.isSuperPrimary }
                        .sortedByDescending { it.isPrimary }
                        .map { it.number }
                        .joinToString(separator = ",")
                )
            }

    fun findAccountsAndGroups(context: Context): ContactAndGroups {
        val contacts = Contacts(context)

        return contacts.accounts().query().find().map { account ->
            val groups = contacts.groups().query().accounts(account).find().map { group ->
                ContactGroup(
                    id = group.id,
                    title = group.title,
                    count = -1
                )
            }

            ContactAccountInfo(
                account = ContactAccount(
                    name = account.name,
                    type = account.type
                ),
                groups = groups
            )
        }.let {
            ContactAndGroups(
                accounts = it
            )
        }
    }

    fun getContactsByAccount(context: Context, name: String, type: String) =
        Contacts(context).query().accounts(Account(name, type))
            .orderBy(ContactsFields.DisplayNamePrimary.asc()).find()
            .flatMap { it.rawContacts }
            .map { rawContact ->
                ContactBasicInfo(
                    id = rawContact.id,
                    contactId = rawContact.contactId,
                    displayNamePrimary = rawContact.name?.displayName,
                    phoneNumber = rawContact.phones.sortedByDescending { it.isSuperPrimary }
                        .sortedByDescending { it.isPrimary }
                        .map { it.number }
                        .joinToString(separator = ",")
                )
            }

    fun getContactsByGroupId(context: Context, groupId: Long) =
        Contacts(context).query().where { GroupMembership.GroupId equalTo groupId }
            .orderBy(ContactsFields.DisplayNamePrimary.asc()).find().flatMap { it.rawContacts }
            .map { rawContact ->
                ContactBasicInfo(
                    id = rawContact.id,
                    contactId = rawContact.contactId,
                    displayNamePrimary = rawContact.name?.displayName,
                    phoneNumber = rawContact.phones.sortedByDescending { it.isSuperPrimary }
                        .sortedByDescending { it.isPrimary }
                        .map { it.number }
                        .joinToString(separator = ",")
                )
            }

    fun getContactDetail(context: Context, id: Long): ContactDetail? {
        val contacts = Contacts(context)
        return contacts.accounts().queryRawContacts().where { Id equalTo id }
            .find()
            .firstOrNull()?.toRawContact(contacts)?.let { convertToContactDetail(context, it) }
    }

    fun convertToContactDetail(context: Context, rawContact: RawContact): ContactDetail {
        val contacts = Contacts(context)

        val phones = rawContact.phones.map { phone ->
            val phoneType = phone.type?.let {
                ContactDataType(
                    value = it.value,
                    typeLabel = it.labelStr(context.resources, null),
                    isUserCustomType = !PhoneEntity.Type.values().contains(it),
                    isSystemCustomType = it == PhoneEntity.Type.CUSTOM
                )
            }

            ContactFieldItem(
                id = phone.id,
                type = phoneType,
                value = phone.number,
            )
        }

        val emails = rawContact.emails.map { email ->
            val emailType = email.type?.let {
                ContactDataType(
                    value = it.value,
                    typeLabel = it.labelStr(context.resources, null),
                    isUserCustomType = !EmailEntity.Type.values().contains(it),
                    isSystemCustomType = it == EmailEntity.Type.CUSTOM
                )
            }

            ContactFieldItem(
                id = email.id,
                type = emailType,
                value = email.address,
            )
        }

        val addresses = rawContact.addresses.map { address ->
            val addressType = address.type?.let {
                ContactDataType(
                    value = it.value,
                    typeLabel = it.labelStr(context.resources, null),
                    isUserCustomType = !AddressEntity.Type.values().contains(it),
                    isSystemCustomType = it == AddressEntity.Type.CUSTOM
                )
            }

            ContactFieldItem(
                id = address.id,
                type = addressType,
                value = address.formattedAddress,
            )
        }

        val ims = rawContact.ims.map { im ->
            val imType = im.type?.let {
                ContactDataType(
                    value = it.value,
                    typeLabel = it.labelStr(context.resources, null),
                    isUserCustomType = !ImEntity.Protocol.values().contains(it),
                    isSystemCustomType = it == ImEntity.Protocol.CUSTOM
                )
            }

            ContactFieldItem(
                id = im.id,
                type = imType,
                value = im.data,
            )
        }

        val relations = rawContact.relations.map { relation ->
            val relationType = relation.type?.let {
                ContactDataType(
                    value = it.value,
                    typeLabel = it.labelStr(context.resources, null),
                    isUserCustomType = !RelationEntity.Type.values().contains(it),
                    isSystemCustomType = it == RelationEntity.Type.CUSTOM
                )
            }

            ContactFieldItem(
                id = relation.id,
                type = relationType,
                value = relation.name,
            )
        }

        val accounts =
            contacts.accounts(rawContact.isProfile).query().associatedWith(rawContact).find()
                .map {
                    ContactAccount(
                        name = it.name,
                        type = it.type
                    )
                }

        val groups = rawContact.groupMemberships.map { groupMembership ->
            contacts.groups().query()
                .where { GroupsFields.Id equalTo (groupMembership.groupId ?: -1L) }.find().first()
                .let {
                    ContactGroup(
                        id = it.id,
                        title = it.title,
                        count = -1
                    )
                }
        }

        val note = rawContact.note?.let {
            ContactNote(
                id = it.id,
                isPrimary = it.isPrimary,
                isSuperPrimary = it.isSuperPrimary,
                note = it.note,
                contactId = it.contactId
            )
        }

        return ContactDetail(
            id = rawContact.id,
            contactId = rawContact.contactId,
            displayNamePrimary = rawContact.name?.displayName,
            phones = phones,
            emails = emails,
            addresses = addresses,
            ims = ims,
            relations = relations,
            accounts = accounts,
            groups = groups,
            note = note
        )
    }
}

package com.youngfeng.android.assistant.server.controller

import android.Manifest
import android.accounts.Account
import com.yanzhenjie.andserver.annotation.PostMapping
import com.yanzhenjie.andserver.annotation.RequestBody
import com.yanzhenjie.andserver.annotation.RequestMapping
import com.yanzhenjie.andserver.annotation.RequestParam
import com.yanzhenjie.andserver.annotation.ResponseBody
import com.yanzhenjie.andserver.annotation.RestController
import com.yanzhenjie.andserver.http.multipart.MultipartFile
import com.youngfeng.android.assistant.app.AirControllerApp
import com.youngfeng.android.assistant.event.Permission
import com.youngfeng.android.assistant.event.RequestPermissionsEvent
import com.youngfeng.android.assistant.server.HttpError
import com.youngfeng.android.assistant.server.HttpModule
import com.youngfeng.android.assistant.server.entity.ContactBasicInfo
import com.youngfeng.android.assistant.server.entity.ContactDataType
import com.youngfeng.android.assistant.server.entity.ContactDataTypeMap
import com.youngfeng.android.assistant.server.entity.ContactDetail
import com.youngfeng.android.assistant.server.entity.HttpResponseEntity
import com.youngfeng.android.assistant.server.request.CreateNewContactRequest
import com.youngfeng.android.assistant.server.request.DeleteRawContactsRequest
import com.youngfeng.android.assistant.server.request.GetContactsByAccountRequest
import com.youngfeng.android.assistant.server.request.IdRequest
import com.youngfeng.android.assistant.server.request.UpdateContactRequest
import com.youngfeng.android.assistant.server.response.ContactAndGroups
import com.youngfeng.android.assistant.server.util.ErrorBuilder
import com.youngfeng.android.assistant.util.ContactUtil
import contacts.core.Contacts
import contacts.core.RawContactsFields
import contacts.core.entities.AddressEntity
import contacts.core.entities.EmailEntity
import contacts.core.entities.GroupMembershipEntity
import contacts.core.entities.ImEntity
import contacts.core.entities.MutableNameEntity
import contacts.core.entities.NameEntity
import contacts.core.entities.NewAddress
import contacts.core.entities.NewEmail
import contacts.core.entities.NewIm
import contacts.core.entities.NewName
import contacts.core.entities.NewNote
import contacts.core.entities.NewPhone
import contacts.core.entities.NewRawContact
import contacts.core.entities.NewRelation
import contacts.core.entities.PhoneEntity
import contacts.core.entities.RelationEntity
import contacts.core.equalTo
import contacts.core.`in`
import contacts.core.util.contact
import contacts.core.util.newMembership
import contacts.core.util.setPhoto
import contacts.core.util.toRawContact
import contacts.ui.entities.AddressTypeFactory
import contacts.ui.entities.EmailTypeFactory
import contacts.ui.entities.ImsTypeFactory
import contacts.ui.entities.PhoneTypeFactory
import contacts.ui.entities.RelationTypeFactory
import org.greenrobot.eventbus.EventBus
import pub.devrel.easypermissions.EasyPermissions
import timber.log.Timber

@RestController
@RequestMapping("/contact")
class ContactController {
    private val mContext by lazy { AirControllerApp.getInstance() }

    @PostMapping("/accountsAndGroups")
    fun accountsAndGroups(): HttpResponseEntity<ContactAndGroups> {
        if (!EasyPermissions.hasPermissions(
                mContext,
                Manifest.permission.GET_ACCOUNTS,
                Manifest.permission.READ_CONTACTS
            )
        ) {
            EventBus.getDefault().post(
                RequestPermissionsEvent(
                    arrayOf(
                        Permission.GetAccounts,
                        Permission.ReadContacts
                    )
                )
            )
            return HttpResponseEntity.commonError(HttpError.LackOfNecessaryPermissions)
        }

        val result = ContactUtil.findAccountsAndGroups(mContext)
        return HttpResponseEntity.success(result)
    }

    @PostMapping("/allContacts")
    fun getAllContacts(): HttpResponseEntity<List<ContactBasicInfo>> {
        if (!EasyPermissions.hasPermissions(mContext, Manifest.permission.READ_CONTACTS)) {
            EventBus.getDefault().post(RequestPermissionsEvent(arrayOf(Permission.ReadContacts)))
            return HttpResponseEntity.commonError(HttpError.LackOfNecessaryPermissions)
        }

        val contacts = ContactUtil.getAllContacts(mContext)
        return HttpResponseEntity.success(contacts)
    }

    @ResponseBody
    @PostMapping("/contactsByAccount")
    fun getContactsByAccount(@RequestBody request: GetContactsByAccountRequest): HttpResponseEntity<List<ContactBasicInfo>> {
        if (!EasyPermissions.hasPermissions(
                mContext,
                Manifest.permission.GET_ACCOUNTS,
                Manifest.permission.READ_CONTACTS
            )
        ) {
            EventBus.getDefault().post(
                RequestPermissionsEvent(
                    arrayOf(
                        Permission.GetAccounts,
                        Permission.ReadContacts
                    )
                )
            )
            return HttpResponseEntity.commonError(HttpError.LackOfNecessaryPermissions)
        }

        val contacts = ContactUtil.getContactsByAccount(mContext, request.name, request.type)
        return HttpResponseEntity.success(contacts)
    }

    @ResponseBody
    @PostMapping("/contactsByGroupId")
    fun getContactsByGroupId(@RequestBody request: IdRequest): HttpResponseEntity<List<ContactBasicInfo>> {
        if (!EasyPermissions.hasPermissions(
                mContext,
                Manifest.permission.GET_ACCOUNTS,
                Manifest.permission.READ_CONTACTS
            )
        ) {
            EventBus.getDefault().post(
                RequestPermissionsEvent(
                    arrayOf(
                        Permission.GetAccounts,
                        Permission.ReadContacts
                    )
                )
            )
            return HttpResponseEntity.commonError(HttpError.LackOfNecessaryPermissions)
        }

        val contacts = ContactUtil.getContactsByGroupId(mContext, request.id)
        return HttpResponseEntity.success(contacts)
    }

    @ResponseBody
    @PostMapping("/contactDetail")
    fun getContactDetailById(@RequestBody request: IdRequest): HttpResponseEntity<ContactDetail> {
        if (!EasyPermissions.hasPermissions(mContext, Manifest.permission.READ_CONTACTS)) {
            EventBus.getDefault().post(RequestPermissionsEvent(arrayOf(Permission.ReadContacts)))
            return HttpResponseEntity.commonError(HttpError.LackOfNecessaryPermissions)
        }

        val contactDetail = ContactUtil.getContactDetail(mContext, request.id)
        return HttpResponseEntity.success(contactDetail)
    }

    @ResponseBody
    @PostMapping("/contactDataTypes")
    fun getContactDataTypes(): HttpResponseEntity<ContactDataTypeMap> {
        if (!EasyPermissions.hasPermissions(mContext, Manifest.permission.READ_CONTACTS)) {
            EventBus.getDefault().post(RequestPermissionsEvent(arrayOf(Permission.ReadContacts)))
            return HttpResponseEntity.commonError(HttpError.LackOfNecessaryPermissions)
        }

        val phoneTypes = PhoneTypeFactory.systemTypes(mContext.resources).map {
            ContactDataType(
                value = it.type.value,
                typeLabel = it.typeLabel,
                isUserCustomType = it.isUserCustomType,
                isSystemCustomType = it.isSystemCustomType
            )
        }

        val emailTypes = EmailTypeFactory.systemTypes(mContext.resources).map {
            ContactDataType(
                value = it.type.value,
                typeLabel = it.typeLabel,
                isUserCustomType = it.isUserCustomType,
                isSystemCustomType = it.isSystemCustomType
            )
        }

        val addressTypes = AddressTypeFactory.systemTypes(mContext.resources).map {
            ContactDataType(
                value = it.type.value,
                typeLabel = it.typeLabel,
                isUserCustomType = it.isUserCustomType,
                isSystemCustomType = it.isSystemCustomType
            )
        }

        val imTypes = ImsTypeFactory.systemTypes(mContext.resources).map {
            ContactDataType(
                value = it.type.value,
                typeLabel = it.typeLabel,
                isUserCustomType = it.isUserCustomType,
                isSystemCustomType = it.isSystemCustomType
            )
        }

        val relationTypes = RelationTypeFactory.systemTypes(mContext.resources).map {
            ContactDataType(
                value = it.type.value,
                typeLabel = it.typeLabel,
                isUserCustomType = it.isUserCustomType,
                isSystemCustomType = it.isSystemCustomType
            )
        }

        return HttpResponseEntity.success(
            ContactDataTypeMap(
                phone = phoneTypes,
                email = emailTypes,
                address = addressTypes,
                im = imTypes,
                relation = relationTypes
            )
        )
    }

    @ResponseBody
    @PostMapping("/createNewContact")
    fun createNewContact(
        @RequestBody request: CreateNewContactRequest
    ): HttpResponseEntity<ContactDetail> {
        if (!EasyPermissions.hasPermissions(mContext, Manifest.permission.WRITE_CONTACTS)) {
            EventBus.getDefault().post(RequestPermissionsEvent(arrayOf(Permission.WriteContacts)))
            return HttpResponseEntity.commonError(HttpError.LackOfNecessaryPermissions)
        }

        val rawContact = NewRawContact()

        val name = request.name
        rawContact.name = NewName(displayName = name)

        val group = request.group

        val contacts = Contacts(mContext)
        if (null != group) {
            val contactsGroup =
                contacts.groups().query().where { Id equalTo group.id }.find()
                    .firstOrNull()

            if (null != contactsGroup) {
                rawContact.groupMemberships = mutableListOf(
                    contactsGroup.newMembership()
                )
            }
        }

        val phones = request.phones
        if (null != phones && phones.isNotEmpty()) {
            rawContact.phones = phones.map { fieldItem ->
                NewPhone(
                    type = PhoneEntity.Type.values()
                        .firstOrNull { it.value == fieldItem.type?.value },
                    label = fieldItem.type?.typeLabel,
                    number = fieldItem.value,
                    normalizedNumber = fieldItem.value
                )
            }.toMutableList()
        }

        val emails = request.emails
        if (null != emails && emails.isNotEmpty()) {
            rawContact.emails = emails.map { fieldItem ->
                NewEmail(
                    type = EmailEntity.Type.values()
                        .firstOrNull { it.value == fieldItem.type?.value },
                    label = fieldItem.type?.typeLabel,
                    address = fieldItem.value,
                )
            }.toMutableList()
        }

        val ims = request.ims
        if (null != ims && ims.isNotEmpty()) {
            rawContact.ims = ims.map { fieldItem ->
                NewIm(
                    protocol = ImEntity.Protocol.values()
                        .firstOrNull { it.value == fieldItem.type?.value },
                    data = fieldItem.value,
                )
            }.toMutableList()
        }

        val addresses = request.addresses
        if (null != addresses && addresses.isNotEmpty()) {
            rawContact.addresses = addresses.map { fieldItem ->
                NewAddress(
                    type = AddressEntity.Type.values()
                        .firstOrNull { it.value == fieldItem.type?.value },
                    label = fieldItem.type?.typeLabel,
                    formattedAddress = fieldItem.value,
                )
            }.toMutableList()
        }

        val relations = request.relations
        if (null != relations && relations.isNotEmpty()) {
            rawContact.relations = relations.map { fieldItem ->
                NewRelation(
                    type = RelationEntity.Type.values()
                        .firstOrNull { it.value == fieldItem.type?.value },
                    label = fieldItem.type?.typeLabel,
                    name = fieldItem.value,
                )
            }.toMutableList()
        }

        val note = request.note

        note?.apply {
            rawContact.note = NewNote(
                note = this
            )
        }

        val account = request.account

        var newAccount: Account? = null
        account?.apply {
            newAccount = Account(
                this.name, this.type
            )
        }

        val newContact = contacts.insert()
            .allowBlanks(true)
            .forAccount(newAccount)
            .rawContacts(rawContact)
            .commit()
            .contact(contacts, rawContact)
            ?: return ErrorBuilder().module(HttpModule.ContactModule)
                .error(HttpError.CreateContactFailure).build()

        return HttpResponseEntity.success(
            ContactUtil.convertToContactDetail(
                mContext,
                newContact.rawContacts.first()
            )
        )
    }

    @ResponseBody
    @PostMapping("/uploadPhotoAndNewContract")
    fun uploadPhotoAndNewContact(
        @RequestParam("avatar") avatar: MultipartFile
    ): HttpResponseEntity<ContactDetail> {
        if (!EasyPermissions.hasPermissions(mContext, Manifest.permission.WRITE_CONTACTS)) {
            EventBus.getDefault().post(RequestPermissionsEvent(arrayOf(Permission.WriteContacts)))
            return HttpResponseEntity.commonError(HttpError.LackOfNecessaryPermissions)
        }

        val contacts = Contacts(mContext)
        val rawContact = NewRawContact()

        val displayName = "User${System.currentTimeMillis()}"
        rawContact.name = NewName(displayName = displayName)

        val newContact = contacts.insert()
            .allowBlanks(true)
            .forAccount(null)
            .rawContacts(rawContact)
            .commit()
            .contact(contacts, rawContact)
            ?: return ErrorBuilder().module(HttpModule.ContactModule)
                .error(HttpError.UploadPhotoAndNewContactFailure).build<ContactDetail>()

        newContact.setPhoto(contacts, avatar.bytes)

        return HttpResponseEntity.success(
            ContactDetail(
                id = newContact.rawContacts.first().id,
                contactId = newContact.id,
                displayNamePrimary = displayName
            )
        )
    }

    @ResponseBody
    @PostMapping("/updatePhotoForContact")
    fun updatePhotoForContact(
        @RequestParam("avatar") avatar: MultipartFile,
        @RequestParam("id") id: Long
    ): HttpResponseEntity<ContactDetail> {
        if (!EasyPermissions.hasPermissions(mContext, Manifest.permission.WRITE_CONTACTS)) {
            EventBus.getDefault().post(RequestPermissionsEvent(arrayOf(Permission.WriteContacts)))
            return HttpResponseEntity.commonError(HttpError.LackOfNecessaryPermissions)
        }

        val contacts = Contacts(mContext)
        val blankRawContact =
            contacts.accounts().queryRawContacts().where { RawContactsFields.Id equalTo id }.find()
                .firstOrNull()
                ?: return ErrorBuilder().module(HttpModule.ContactModule)
                    .error(HttpError.ContactNotFound).build()

        val rawContact = blankRawContact.toRawContact(contacts) ?: return ErrorBuilder().module(
            HttpModule.ContactModule
        )
            .error(HttpError.ContactNotFound).build()

        val isSuccess = rawContact.setPhoto(contacts, avatar.bytes)

        Timber.e("上传图片结果: $isSuccess");

        return if (isSuccess) {
            HttpResponseEntity.success(
                ContactUtil.convertToContactDetail(mContext, rawContact)
            )
        } else {
            ErrorBuilder().module(HttpModule.ContactModule)
                .error(HttpError.UpdatePhotoFailure).build()
        }
    }

    @ResponseBody
    @PostMapping("/updateContact")
    fun updateContact(
        @RequestBody request: UpdateContactRequest
    ): HttpResponseEntity<Any> {
        if (!EasyPermissions.hasPermissions(mContext, Manifest.permission.WRITE_CONTACTS)) {
            EventBus.getDefault().post(RequestPermissionsEvent(arrayOf(Permission.WriteContacts)))
            return HttpResponseEntity.commonError(HttpError.LackOfNecessaryPermissions)
        }

        val contacts = Contacts(mContext)

        val blankRawContact =
            contacts.accounts().queryRawContacts().where { RawContactsFields.Id equalTo request.id }
                .find()
                .firstOrNull()
                ?: return ErrorBuilder().module(HttpModule.ContactModule)
                    .error(HttpError.RawContactNotFound).build()

        val rawContact =
            blankRawContact.toRawContact(contacts)?.mutableCopy() ?: return ErrorBuilder().module(
                HttpModule.ContactModule
            )
                .error(HttpError.RawContactNotFound).build()

        val username = request.name

        val nameEntity: NameEntity = NewName()
        username.apply {
            nameEntity.applyIfMutable {
                givenName = username
                rawContact.name = this
            }
        }

        rawContact.phones = request.phones?.map { phone ->
            val oldPhone = rawContact.phones.firstOrNull { it.idOrNull == phone.id }

            oldPhone?.apply {
                type = PhoneEntity.Type.values()
                    .firstOrNull { it.value == phone.type?.value }
                number = phone.value
            }
                ?: NewPhone(
                    type = PhoneEntity.Type.values()
                        .firstOrNull { it.value == phone.type?.value },
                    label = phone.type?.typeLabel,
                    number = phone.value,
                    normalizedNumber = phone.value
                )
        }?.toMutableList() ?: mutableListOf()

        rawContact.emails = request.emails?.map { email ->
            val oldEmail = rawContact.emails.firstOrNull { it.idOrNull == email.id }

            oldEmail?.apply {
                type = EmailEntity.Type.values()
                    .firstOrNull { it.value == email.type?.value }
                address = email.value
            }
                ?: NewEmail(
                    type = EmailEntity.Type.values()
                        .firstOrNull { it.value == email.type?.value },
                    label = email.type?.typeLabel,
                    address = email.value,
                )
        }?.toMutableList() ?: mutableListOf()

        rawContact.addresses = request.addresses?.map { address ->
            val oldAddress = rawContact.addresses.firstOrNull { it.idOrNull == address.id }

            oldAddress?.apply {
                type = AddressEntity.Type.values()
                    .firstOrNull { it.value == address.type?.value }
                formattedAddress = address.value
            }
                ?: NewAddress(
                    type = AddressEntity.Type.values()
                        .firstOrNull { it.value == address.type?.value },
                    label = address.type?.typeLabel,
                    formattedAddress = address.value,
                )
        }?.toMutableList() ?: mutableListOf()

        rawContact.ims = request.ims?.map { im ->
            val oldIm = rawContact.ims.firstOrNull { it.idOrNull == im.id }

            oldIm?.apply {
                type = ImEntity.Protocol.values()
                    .firstOrNull { it.value == im.type?.value }
                data = im.value
            }
                ?: NewIm(
                    protocol = ImEntity.Protocol.values()
                        .firstOrNull { it.value == im.type?.value },
                    data = im.value,
                )
        }?.toMutableList() ?: mutableListOf()

        rawContact.relations = request.relations?.map { relation ->
            val oldRelation = rawContact.relations.firstOrNull { it.idOrNull == relation.id }

            oldRelation?.apply {
                type = RelationEntity.Type.values()
                    .firstOrNull { it.value == relation.type?.value }
                name = relation.value
            }
                ?: NewRelation(
                    type = RelationEntity.Type.values()
                        .firstOrNull { it.value == relation.type?.value },
                    name = relation.value,
                )
        }?.toMutableList() ?: mutableListOf()

        request.note?.also { note ->
            rawContact.note?.apply { this.note = note } ?: run {
                rawContact.note = NewNote(
                    note = note
                )
            }
        }

        request.group?.let { group ->
            contacts.groups().query().where { Id equalTo group.id }.find()
                .firstOrNull()
        }?.apply {
            val memberships: MutableList<GroupMembershipEntity> = mutableListOf()
            memberships.add(this.newMembership())
            rawContact.groupMemberships = memberships
        }

        val isSuccessful = contacts.update().rawContacts(rawContact)
            .commit()
            .isSuccessful

        if (!isSuccessful) {
            return ErrorBuilder().module(HttpModule.ContactModule)
                .error(HttpError.CreateContactFailure).build()
        }

        return HttpResponseEntity.success()
    }

    private fun NameEntity.applyIfMutable(block: MutableNameEntity.() -> Unit) {
        if (this is MutableNameEntity) {
            block(this)
        }
    }

    @ResponseBody
    @PostMapping("/deleteRawContact")
    fun deleteRawContacts(@RequestBody request: DeleteRawContactsRequest): HttpResponseEntity<Any> {
        if (!EasyPermissions.hasPermissions(mContext, Manifest.permission.WRITE_CONTACTS)) {
            EventBus.getDefault().post(RequestPermissionsEvent(arrayOf(Permission.WriteContacts)))
            return HttpResponseEntity.commonError(HttpError.LackOfNecessaryPermissions)
        }

        return Contacts(mContext).delete()
            .rawContactsWhere { Id `in` request.ids }
            .commit().isSuccessful.let { isSuccessful ->
                if (isSuccessful) {
                    HttpResponseEntity.success()
                } else {
                    ErrorBuilder().module(HttpModule.ContactModule)
                        .error(HttpError.DeleteRawContactsFailure).build()
                }
            }
    }
}

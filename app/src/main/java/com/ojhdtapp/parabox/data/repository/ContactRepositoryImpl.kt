package com.ojhdtapp.parabox.data.repository

import android.content.Context
import com.ojhdtapp.parabox.core.util.Resource
import com.ojhdtapp.parabox.data.local.AppDatabase
import com.ojhdtapp.parabox.domain.model.Contact
import com.ojhdtapp.parabox.domain.repository.ContactRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class ContactRepositoryImpl @Inject constructor(
    val context: Context,
    private val db: AppDatabase,
) : ContactRepository {
    override fun queryContact(query: String): Flow<Resource<List<Contact>>> {
        TODO("Not yet implemented")
    }

    override fun getContactById(contactId: Long): Flow<Resource<Contact>> {
        return flow {
            emit(Resource.Loading())
            try {
                emit(db.contactDao.getContactById(contactId)?.toContact()?.let {
                    Resource.Success(it)
                } ?: Resource.Error("not found"))
            } catch (e: Exception) {
                emit(Resource.Error("unknown error"))
            }
        }
    }
}
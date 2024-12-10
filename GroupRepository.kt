class GroupRepository(private val db: SQLiteDatabase) {
    fun addContactToGroup(contactId: Int, groupId: Int) {
        val values = ContentValues().apply {
            put("contact_id", contactId)
            put("group_id", groupId)
        }
        db.insert("contact_groups", null, values)
    }
} 

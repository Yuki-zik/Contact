class ContactRepository(private val db: SQLiteDatabase) {
    fun addContact(contact: Contact) {
        // 插入联系人基本信息
        val values = ContentValues().apply {
            put("name", contact.name)
            put("phone", contact.phone)
            put("company", contact.company)
            put("email", contact.email)
            put("ringtone", contact.ringtone)
        }
        db.insert("contacts", null, values)
    }
    
    fun getContactsByGroup(groupId: Int): List<Contact> {
        // 通过群组ID查询联系人
        return db.rawQuery("""
            SELECT c.* FROM contacts c
            INNER JOIN contact_groups cg ON c.id = cg.contact_id
            WHERE cg.group_id = ?
        """, arrayOf(groupId.toString())).use { cursor ->
            // 解析结果...
        }
    }
} 

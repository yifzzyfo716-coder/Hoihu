package com.example.data.repository

import com.example.data.model.CameraCategory
import com.example.data.model.CctvCamera

object CctvRepository {

    // กล้องวงจรปิดเรียลไทม์แท้ 4 จุด ตามที่ผู้ใช้ระบุโดยเฉพาะ (ไม่มีการจำลอง)
    val cameras: List<CctvCamera> = listOf(
        CctvCamera(
            id = "crystal_bay",
            name = "Crystal Bay Beach Resort | Lamai | Koh Samui",
            thaiName = "หาดคริสตัลเบย์ (อ่าวท้องตะเคียน) หาดละไม",
            location = "Crystal Bay Beach Resort, หาดละไม, เกาะสมุย, สุราษฎร์ธานี",
            area = "หาดละไม / อ่าวท้องตะเคียน",
            youtubeVideoId = "Fw9hgttWzIg",
            originalUrl = "https://www.youtube.com/live/Fw9hgttWzIg?si=bz-wGB4LvzslUB-j",
            latitude = 9.51654,
            longitude = 100.06452,
            description = "ภาพสดชายหาดคริสตัลเบย์ (Silver Beach) น้ำทะเลใสโขดหินแกรนิตธรรมชาติ เหมาะสำหรับพักผ่อนและว่ายน้ำ",
            category = CameraCategory.BEACH
        ),
        CctvCamera(
            id = "baobab_lamai",
            name = "Baobab Cam | Lamai | Koh Samui | Thailand",
            thaiName = "ร้านอาหาร Baobab ชายหาดละไม",
            location = "Baobab Restaurant, ชายหาดละไมฝั่งใต้, เกาะสมุย, สุราษฎร์ธานี",
            area = "หาดละไมฝั่งใต้",
            youtubeVideoId = "Tpj0cmMVOd0",
            originalUrl = "https://www.youtube.com/live/Tpj0cmMVOd0?si=WzMMAo6nAfnsagxU",
            latitude = 9.46732,
            longitude = 100.04635,
            description = "มุมมองสดชายหาดหน้าร้านอาหาร Baobab สัมผัสคลื่นลมทะเลละไม ต้นมะพร้าว และผู้คนริมหาดแบบเรียลไทม์",
            category = CameraCategory.RESTAURANT
        ),
        CctvCamera(
            id = "bondi_aussie",
            name = "Bondi Aussie Bar & Grill | Lamai | Koh Samui",
            thaiName = "Bondi Aussie Bar & Grill ถนนคนเดินละไม",
            location = "Bondi Aussie Bar, ถนนคนเดินหาดละไม, เกาะสมุย, สุราษฎร์ธานี",
            area = "ย่านบันเทิง/ถนนคนเดินหาดละไม",
            youtubeVideoId = "-lI2AH2WkEA",
            originalUrl = "https://www.youtube.com/live/-lI2AH2WkEA?si=zH3IR-dPJCqF4QHn",
            latitude = 9.47055,
            longitude = 100.04851,
            description = "กล้องตรวจการณ์การจราจรและบรรยากาศยามเย็น-ค่ำคืนใจกลางถนนคนเดินและย่านบันเทิงหาดละไม",
            category = CameraCategory.STREET
        ),
        CctvCamera(
            id = "el_gaucho_bophut",
            name = "El Gaucho Fisherman's Village Bophut | Koh Samui",
            thaiName = "El Gaucho หมู่บ้านชาวประมง บ่อผุด",
            location = "El Gaucho, Fisherman's Village, ต.บ่อผุด, เกาะสมุย, สุราษฎร์ธานี",
            area = "Fisherman's Village บ่อผุด",
            youtubeVideoId = "CSp55hSd_6A",
            originalUrl = "https://www.youtube.com/live/CSp55hSd_6A?si=-ywJih2nKHe_CoiH",
            latitude = 9.55938,
            longitude = 100.03061,
            description = "ภาพสดบรรยากาศถนนเลียบทะเลและทางเดินประวัติศาสตร์ Fisherman's Village บ่อผุด มองเห็นวิวทะเลอ่าวไทยสู่เกาะพะงัน",
            category = CameraCategory.STREET
        )
    )

    fun getCameraById(id: String): CctvCamera? = cameras.find { it.id == id }
}

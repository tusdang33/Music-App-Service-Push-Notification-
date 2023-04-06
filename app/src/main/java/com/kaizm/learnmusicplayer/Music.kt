package com.kaizm.learnmusicplayer

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.Serializable

class Music(val id: Int, val name: String, val author: String, val img: Bitmap?, val music: Int) :
    Serializable {
    companion object {
        fun getList(): List<Music> {
            val m1 = Music(
                1,
                "Alone and Forsaken",
                "HANK WILLIAMS",
                bitMap(R.drawable.alone_and_forsaken),
                R.raw.alone_and_forsaken
            )
            val m2 = Music(
                2,
                "Save your Tear",
                "The Weeknd",
                bitMap(R.drawable.syt),
                R.raw.save_your_tear
            )
            val m3 =
                Music(3, "Waiting for you", "Mono", bitMap(R.drawable.wtfy), R.raw.waitng_for_you)
            return listOf(m1, m2, m3)
        }

        private fun bitMap(source: Int) =
            BitmapFactory.decodeResource(Resources.getSystem(), source)
    }
}

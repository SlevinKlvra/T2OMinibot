package com.intec.telemedicina.robotinterface
import android.content.Context
import android.content.SharedPreferences
import com.ainirobot.coreservice.client.actionbean.Pose
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
class SavePosesSharedPreferences (private val context: Context) {

    private val sharedPreferences: SharedPreferences by lazy {
        context.getSharedPreferences("pose_shared_prefs", Context.MODE_PRIVATE)
    }

    fun saveDataList(dataList: List<Pose>) {
        val editor = sharedPreferences.edit()
        val json = Gson().toJson(dataList)
        editor.putString("data_poses", json)
        editor.apply()
    }

    fun getDataList(): List<Pose> {
        val json = sharedPreferences.getString("data_poses", null)
        return if (json != null) {
            val type = object : TypeToken<List<Pose>>() {}.type
            Gson().fromJson(json, type)
        } else {
            emptyList()
        }
    }
}

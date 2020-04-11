package jp.techacademy.chiba.qa_app

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView

import java.util.ArrayList

class FavoritesListAdapter(context: Context) : BaseAdapter() {
    private var mFLayoutInflater: LayoutInflater
    private var mFQuestionArrayList = ArrayList<Question>()

    init {
        mFLayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    }

    override fun getCount(): Int{
        return mFQuestionArrayList.size
    }

    override fun getItem(position: Int): Any{
        return mFQuestionArrayList[position]
    }

    override fun getItemId(position: Int): Long{
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View{
        var convertView = convertView

        if (convertView == null){
            convertView = mFLayoutInflater.inflate(R.layout.list_favorites, parent, false)
        }

        val titleText = convertView!!.findViewById<View>(R.id.favtitleTextView) as TextView
        titleText.text = mFQuestionArrayList[position].title
        Log.d("test","タイトル" + mFQuestionArrayList[position].title)

        val nameText = convertView.findViewById<View>(R.id.favnameTextView) as TextView
        nameText.text = mFQuestionArrayList[position].name

        val resText = convertView.findViewById<View>(R.id.favResTextView) as TextView
        val resNum = mFQuestionArrayList[position].answers.size
        resText.text = resNum.toString()

        val bytes = mFQuestionArrayList[position].imageBytes
        if (bytes.isNotEmpty()){
            val image = BitmapFactory.decodeByteArray(bytes,0, bytes.size).copy(Bitmap.Config.ARGB_8888, true)
            val imageView = convertView.findViewById<View>(R.id.favimageView) as ImageView
            imageView.setImageBitmap(image)
        }

        return  convertView
    }

    fun setFQuestionArrayList(questionArrayList: ArrayList<Question>){
        mFQuestionArrayList = questionArrayList
    }
}
package jp.techacademy.chiba.qa_app

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.design.widget.Snackbar
import android.view.View
import android.view.inputmethod.InputMethodManager

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_answer_send.*

import java.util.HashMap

class AnswerSendActivity : AppCompatActivity(), View.OnClickListener, DatabaseReference.CompletionListener {

    private lateinit var mQuestion: Question

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_answer_send)

        //渡ってきたquestionのオブジェクトを保持する
        val extras = intent.extras
        mQuestion = extras.get("question") as Question

        //UIの準備
        sendButton.setOnClickListener(this)
    }

    override fun onComplete(databaseError: DatabaseError?, dataReference: DatabaseReference) {
        progressBar.visibility = View.GONE

        if (databaseError == null){
            finish()
        }else{
            Snackbar.make(findViewById(android.R.id.content), "投稿に失敗しました", Snackbar.LENGTH_LONG).show()
        }

    }

    override fun onClick(v: View){
        //キーボード閉じる
        val im = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        im.hideSoftInputFromWindow(v.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)

        val dataBaseReference = FirebaseDatabase.getInstance().reference
        val answerRef = dataBaseReference.child(ContentsPATH).child(mQuestion.genre.toString()).child(mQuestion.questionUid).child(AnswersPATH)

        val data = HashMap<String, String>()

        //UID
        data["uid"] = FirebaseAuth.getInstance().currentUser!!.uid

        //表示名
        //preferenceから名前とる
        val sp = PreferenceManager.getDefaultSharedPreferences(this)
        val name = sp.getString(NameKEY, "")
        data["name"] = name

        //回答を取得する
        val answer= answerEditText.text.toString()

        if(answer.isEmpty()){
            //回答が入力されていないときはエラーを表示するだけ
            Snackbar.make(v, "回答を入力してください", Snackbar.LENGTH_LONG).show()
            return
        }

        data["body"] = answer

        progressBar.visibility = View.VISIBLE
        answerRef.push().setValue(data, this)
    }
}

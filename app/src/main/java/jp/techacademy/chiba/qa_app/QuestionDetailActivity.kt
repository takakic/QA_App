package jp.techacademy.chiba.qa_app

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.util.Log
import android.view.View
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_question_detail.*
import kotlinx.android.synthetic.main.activity_question_send.*

class QuestionDetailActivity : AppCompatActivity() {

    private lateinit var mQuestion: Question
    private lateinit var mAdapter: QuestionDetailListAdapter
    private lateinit var mAnswerRef: DatabaseReference
    private var mFavoriteFlag = false

    private val mEventListener = object : ChildEventListener{
        override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?){
            val map = dataSnapshot.value as Map<String, String>

            val answerUid = dataSnapshot.key ?: ""

            for (answer in mQuestion.answers){
                //同じansweruidのものが存在しているときは何もしない
                if (answerUid == answer.answerUid){
                    return
                }
            }

            val body = map["body"] ?: ""
            val name = map["name"] ?: ""
            val uid = map["uid"] ?: ""

            val answer = Answer(body, name, uid, answerUid)
            mQuestion.answers.add(answer)
            mAdapter.notifyDataSetChanged()
        }

        override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {

        }

        override fun onChildRemoved(dataSnapshot: DataSnapshot) {

        }

        override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {

        }

        override fun onCancelled(databaseError: DatabaseError) {

        }
    }

    private val favoriteEventListener = object : ChildEventListener{
        override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
            val data = HashMap<String, String>()
            data["mGenre"] = mQuestion.genre.toString()

            if(data["mGenre"] != null){
                favoriteButton.setImageResource(R.drawable.heart_button_selected)
                mFavoriteFlag = true
            }else{
                favoriteButton.setImageResource(R.drawable.heart_button_unselected)
                mFavoriteFlag = false
            }
        }

        override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {
            val data = HashMap<String, String>()
            data["mGenre"] = mQuestion.genre.toString()

            if(data["mGenre"] != null){
                favoriteButton.setImageResource(R.drawable.heart_button_selected)
                mFavoriteFlag = true
            }else{
                favoriteButton.setImageResource(R.drawable.heart_button_unselected)
                mFavoriteFlag = false
            }

        }

        override fun onChildRemoved(dataSnapshot: DataSnapshot) {

        }

        override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {

        }

        override fun onCancelled(databaseError: DatabaseError) {

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_question_detail)

        //渡ってきたquestionのオブジェクトを保持する
        val extras = intent.extras
        mQuestion = extras.get("question") as Question

        title = mQuestion.title

        //ListViewの準備
        mAdapter = QuestionDetailListAdapter(this, mQuestion)
        listView.adapter = mAdapter
        mAdapter.notifyDataSetChanged()

        //ログイン状態チェック
        val user = FirebaseAuth.getInstance().currentUser

        if (user == null){
            //お気に入りボタンを隠す
            favoriteButton.hide()
        }else{
            //お気に入りの状態を確認
            //Firebaseのfavoritesから、questionIDにいき、favoriteの中身を確認する
            val databaseReference = FirebaseDatabase.getInstance().reference

            val favoriteRef = databaseReference.child(FavoritePATH).child(user!!.uid).child(mQuestion.questionUid)
            favoriteRef.addChildEventListener(favoriteEventListener)
        }

        fab.setOnClickListener{
            //ログイン済みのユーザーを取得する
            val user = FirebaseAuth.getInstance().currentUser

            if (user == null){
                //ログインしていなければログイン画面に遷移する
                val intent = Intent(applicationContext, LoginActivity::class.java)
                startActivity(intent)
            }else{
                //Questionを渡して回答作成画面を起動する
                val intent = Intent(applicationContext, AnswerSendActivity::class.java)
                intent.putExtra("question", mQuestion)
                startActivity(intent)
            }

            val dataBaseReference = FirebaseDatabase.getInstance().reference
            mAnswerRef = dataBaseReference.child(ContentsPATH).child(mQuestion.genre.toString()).child(mQuestion.questionUid).child(AnswersPATH)
            mAnswerRef.addChildEventListener(mEventListener)

        }

        favoriteButton.setOnClickListener{
            //課題、お気に入りボタンを押したときのアクション
            val user = FirebaseAuth.getInstance().currentUser

            val databaseReference = FirebaseDatabase.getInstance().reference
            val favoriteRef = databaseReference.child(FavoritePATH).child(user!!.uid).child(mQuestion.questionUid)
            val data = HashMap<String, String>()

            //お気に入り登録されているか確認
            if(mFavoriteFlag == false){ //お気に入り対象に追加する。db呼び出して、登録する
                data["mGenre"] = mQuestion.genre.toString()
                favoriteRef.setValue(data) //決まった値であれば、setValue(data) push()は自動でIDで付与
                    mFavoriteFlag = true
            }else{
                //データ解除　
                databaseReference.child(FavoritePATH).child(user!!.uid).child(mQuestion.questionUid).removeValue()
                mFavoriteFlag = false
                favoriteButton.setImageResource(R.drawable.heart_button_unselected)

            }
        }
    }
}

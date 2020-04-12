package jp.techacademy.chiba.qa_app

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.util.Base64
import android.util.Log
import android.widget.ListView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class FavoritesActivity : AppCompatActivity() {
    private lateinit var mToolbar: Toolbar
    private var mFavRef: DatabaseReference? = null
    private lateinit var mDatabaseReference: DatabaseReference
    private lateinit var mQuestionArrayList: ArrayList<Question>
    private lateinit var mAdapter: QuestionListAdapter
    private lateinit var mListView: ListView

    private val mEventListener = object : ChildEventListener {
        override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?){
            val map = dataSnapshot.value as Map<String, String>
            val databaseReference = FirebaseDatabase.getInstance().reference
            val genre = Integer.parseInt(map["mGenre"])
            val questionUid = dataSnapshot.key

            val ref = databaseReference.child(ContentsPATH).child(genre.toString()).child(questionUid!!.toString())

            ref.addListenerForSingleValueEvent(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot){
                    val data = snapshot.value as Map<String, String>
                    val title = data["title"] ?: ""
                    val body = data["body"] ?: ""
                    val name = data["name"] ?: ""
                    val uid = data["uid"] ?: ""
                    val imageString = data["image"] ?: ""
                    val bytes =
                        if(imageString.isNotEmpty()){
                            Base64.decode(imageString, Base64.DEFAULT)
                        }else{
                            byteArrayOf()
                        }

                    val answerArrayList = ArrayList<Answer>()
                    val answerMap = data["answers"] as Map<String, String>?
                    if(answerMap != null){
                        for(key in answerMap.keys){
                            val temp = answerMap[key] as Map<String, String>
                            val answerBody = temp["body"] ?: ""
                            val answerName = temp["name"] ?: ""
                            val answerUid = temp["uid"] ?: ""
                            val answer = Answer(answerBody, answerName, answerUid, key)
                            answerArrayList.add(answer)
                        }
                    }

                    val question = Question(title, body, name, uid, snapshot.key ?: "", genre, bytes, answerArrayList)
                    mQuestionArrayList.add(question)
                    mAdapter.notifyDataSetChanged()

                }
                override fun onCancelled(firebaseError: DatabaseError){}
            })

        }

        override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?){

        }

        override fun onChildRemoved(p0: DataSnapshot) {

        }

        override fun onChildMoved(p0: DataSnapshot, p1: String?) {

        }

        override fun onCancelled(p0: DatabaseError) {

        }
    }

    override fun onResume() {
        super.onResume()
        //Firebase
        mDatabaseReference = FirebaseDatabase.getInstance().reference

        mQuestionArrayList.clear()
        mAdapter.setQuestionArrayList(mQuestionArrayList)
        mListView.adapter = mAdapter

        val mUser = FirebaseAuth.getInstance().currentUser

        mFavRef = mDatabaseReference.child(FavoritePATH).child(mUser!!.uid)
        mFavRef!!.addChildEventListener(mEventListener)

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favorites)

        title = "♡お気に入り"

        //Listviewの準備
        mListView = findViewById(R.id.listView)
        mAdapter = QuestionListAdapter(this)
        mQuestionArrayList = ArrayList<Question>()

        //Questionのインスタンスを渡して質問詳細画面を起動する
        mListView.setOnItemClickListener{ parent, view, position, id ->
            val intent = Intent(applicationContext, QuestionDetailActivity::class.java)
            intent.putExtra("question", mQuestionArrayList[position])
            startActivity(intent)
        }
    }
}

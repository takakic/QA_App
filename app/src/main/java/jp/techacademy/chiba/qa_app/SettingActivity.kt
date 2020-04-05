package jp.techacademy.chiba.qa_app

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.database.DatabaseReference
import android.support.design.widget.Snackbar
import com.google.firebase.auth.FirebaseAuth
import android.preference.PreferenceManager
import android.view.inputmethod.InputMethodManager
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_setting.*

class SettingActivity : AppCompatActivity() {

    private lateinit var mDatabaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)

        //prederenceからヒョ自明を取得してedittextに反映させる
        val sp = PreferenceManager.getDefaultSharedPreferences(this)
        val name = sp.getString(NameKEY, "")
        nameText.setText(name)

        mDatabaseReference = FirebaseDatabase.getInstance().reference

        //UIの初期設定
        title = "設定"

        changeButton.setOnClickListener{ v ->
            //キーボード収納
            val im = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            im.hideSoftInputFromWindow(v.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)

            //ログイン済みのユーザーを取得
            val user = FirebaseAuth.getInstance().currentUser

            if(user == null){
                //ログインしていない場合はなにもしない
                Snackbar.make(v, "ログインしていません", Snackbar.LENGTH_LONG).show()

            }else{
                //変更した表示名をFirebaseに保存する
                val name = nameText.text.toString()
                val userRef = mDatabaseReference.child(UsersPATH).child(user.uid)
                val data = HashMap<String, String>()
                data["name"] = name
                userRef.setValue(data)

                //変更した表示名をPReferenceに保存する
                val sp = PreferenceManager.getDefaultSharedPreferences(applicationContext)
                val editor = sp.edit()
                editor.commit()

                Snackbar.make(v, "表示名を変更しました", Snackbar.LENGTH_LONG).show()
            }
        }

        logoutButton.setOnClickListener{ v ->
            FirebaseAuth.getInstance().signOut()
            nameText.setText("")
            Snackbar.make(v, "ログアウトしました", Snackbar.LENGTH_LONG).show()
        }
    }
}

package trycatch.dev.buscomplain.View.Activity

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_searchable.*
import kotlinx.android.synthetic.main.content_compliment.*
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.error
import trycatch.dev.buscomplain.R
import trycatch.dev.buscomplain.SendApi
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class ComplimentActivity : AppCompatActivity(), AnkoLogger {
    private val sendApi by lazy {
        SendApi.create()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_compliment)
        setSupportActionBar(toolbar)

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayHomeAsUpEnabled(true)
            title = "칭찬하기"
        }

        company_input.setText(intent.getStringExtra("company").split(" ")[0])
        line_input.setText(intent.getStringExtra("busNumber"))
        car_number_input.setText(intent.getStringExtra("carNumber"))
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.send, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home ->
                finish()
            R.id.send -> {
                sendApi.sendSeoulCompliment(
                        URLEncoder.encode(title_input.text.toString(), "euc-kr"),
                        URLEncoder.encode(name_input.text.toString(), "euc-kr"),
                        URLEncoder.encode(company_input.text.toString(), "euc-kr"),
                        URLEncoder.encode(line_input.text.toString(), "euc-kr"),
                        "",
                        "123456",
                        URLEncoder.encode("차량번호:${car_number_input.text.toString()}\n${remarks_input.text.toString()}", "euc-kr"),
                        "",
                        "",
                        "1",
                        "",
                        "",
                        "write"
                ).subscribe({
                    error { it }
                }) {
                    it.printStackTrace()
                }
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
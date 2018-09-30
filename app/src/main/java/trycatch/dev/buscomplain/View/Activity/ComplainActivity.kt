package trycatch.dev.buscomplain.View.Activity

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_complain.*
import kotlinx.android.synthetic.main.content_complain.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.error
import trycatch.dev.buscomplain.R
import trycatch.dev.buscomplain.SendApi
import java.net.URLEncoder

class ComplainActivity : AppCompatActivity(), AnkoLogger {
    private val sendApi by lazy {
        SendApi.create()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_complain)
        setSupportActionBar(toolbar)

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayHomeAsUpEnabled(true)
            title = "건의하기"
        }

        company_input.setText(intent.getStringExtra("company").split(" ")[0])
        line_input.setText(intent.getStringExtra("busNumber"))
        car_number_input.setText(intent.getStringExtra("carNumber"))

        division_select.adapter = ArrayAdapter.createFromResource(
                this,
                R.array.division_array,
                android.R.layout.simple_spinner_item
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
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
                sendApi.sendSeoulComplain(
                        URLEncoder.encode(title_input.text.toString(), "euc-kr"),
                        (division_select.selectedItemPosition + 1).toString(),
                        phone1_num_input.text.toString(),
                        phone2_num_input.text.toString(),
                        phone3_num_input.text.toString(),
                        URLEncoder.encode(name_input.text.toString(), "euc-kr"),
                        URLEncoder.encode(company_input.text.toString(), "euc-kr"),
                        URLEncoder.encode(line_input.text.toString(), "euc-kr"),
                        "",
                        "123456",
                        URLEncoder.encode("차량번호:${car_number_input.text.toString()}<br>${remarks_input.text.toString()}", "euc-kr"),
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
package com.norm.myqrscannercompose

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import com.norm.myqrscannercompose.data.MainDb
import com.norm.myqrscannercompose.data.Product
import com.norm.myqrscannercompose.ui.theme.MyQrScannerComposeTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var mainDb: MainDb

    @OptIn(DelicateCoroutinesApi::class)
    private val scanLauncher = registerForActivityResult(ScanContract()) { result ->
        if (result.contents == null) {
            Toast.makeText(
                this,
                "Scan data is null!",
                Toast.LENGTH_SHORT
            )
                .show()
        } else {
            CoroutineScope(Dispatchers.IO).launch {
                val productByQr = mainDb.dao.getProductByQr(result.contents)
                if (productByQr == null) {
                    mainDb.dao.insertProduct(
                        Product(
                            null,
                            "QR data",
                            result.contents
                        )
                    )
                    runOnUiThread {
                        Toast.makeText(
                            this@MainActivity,
                            "Item saved!",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(
                            this@MainActivity,
                            "Duplicated item!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }

    private val scanCheckLauncher = registerForActivityResult(ScanContract()) { result ->
        if (result.contents == null) {
            Toast.makeText(
                this,
                "Scan data is null!",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            CoroutineScope(Dispatchers.IO).launch {
                val productByQr = mainDb.dao.getProductByQr(result.contents)
                if (productByQr == null) {
                    runOnUiThread {
                        Toast.makeText(
                            this@MainActivity,
                            "Product not added! ",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    mainDb.dao.updateProduct(productByQr.copy(isChecked = true))
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val productStateList = mainDb.dao.getAllProducts().collectAsState(initial = emptyList())
            MyQrScannerComposeTheme {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .padding(top = 8.dp, start = 4.dp, end = 4.dp)
                            .fillMaxWidth()
                            .fillMaxHeight(0.9f)
                    ) {
                        items(productStateList.value) { product ->
                            Spacer(modifier = Modifier.height(8.dp))
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 8.dp, end = 8.dp),
                                colors = CardDefaults.cardColors(
                                    contentColor = Color.White,
                                    containerColor = if (product.isChecked) {
                                        Color.Blue
                                    } else {
                                        Color.Red
                                    }
                                )
                            ) {
                                Text(
                                    text = product.numberQr,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(onClick = {
                            scanCheck()
                        }) {
                            Text(text = "Check QR")
                        }
                        Button(onClick = {
                            scan()
                        }) {
                            Text(text = "Scan QR")
                        }
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(onClick = {
                            restCheck(productStateList.value)
                        }) {
                            Text(text = "Rest check")
                        }
                        Button(onClick = {
                            cleanList(productStateList.value)
                        }) {
                            Text(text = "Clean list")
                        }
                    }
                }
            }
        }
    }

    private fun restCheck(products: List<Product>) {
        CoroutineScope(Dispatchers.IO).launch {
            products.forEach { product ->
                mainDb.dao.updateProduct(product.copy(isChecked = false))
            }
        }
    }

    private fun cleanList(products: List<Product>) {
        CoroutineScope(Dispatchers.IO).launch {
            products.forEach { product ->
                mainDb.dao.deleteProduct(product.copy(isChecked = false))
            }
        }
    }

    private fun scan() {
        scanLauncher.launch(getScanOption())
    }

    private fun scanCheck() {
        scanCheckLauncher.launch(getScanOption())
    }

    private fun getScanOption(): ScanOptions {
        return ScanOptions().apply {
            setDesiredBarcodeFormats(ScanOptions.QR_CODE)
            setPrompt("Scan a QR COD")
            setCameraId(0) // Use a specific camera of the device

            setBeepEnabled(false)
            setBarcodeImageEnabled(true)
        }
    }
}
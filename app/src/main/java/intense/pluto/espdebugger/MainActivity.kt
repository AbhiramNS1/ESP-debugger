package intense.pluto.espdebugger

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHost
import androidx.navigation.fragment.NavHostFragment.Companion.findNavController
import intense.pluto.espdebugger.ui.theme.ESPDebuggerTheme


class MainActivity : ComponentActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ESPDebuggerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {

                    LazyColumn() {
                        item {
                            GridBox(4, 20)
                        }
                    }

                }
            }
        }




    }

    // This helps to handle
    // the permission request results .
    // when the user deny or grand the permission this function will be called

    val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
                isGranted: Boolean ->
            if (isGranted){
                AskUserToTurnOn()
            }else {
                showPermissionAlert()
                Log.d("------------permission------","No course fine access")

            }
        }


    fun checkBluetoothPermission() {

            if(
                ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
            ){
                        AskUserToTurnOn()
            }
            else if(shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) ){
                showPermissionAlert()
                Log.d("------------permission------","No course fine access")

            }
            else requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }





    fun showTurnONAlertDialog() {
        AlertDialog.Builder(this)
            .setTitle("Bluetooth has to be turned on")
            .setMessage("This app needs Bluetooth to function properly.")
            .setPositiveButton("OK") { _, _ ->
                AskUserToTurnOn()
            }
            .setNegativeButton("Cancel") { _, _ ->
               finish()
            }
            .create()
            .show()
    }


    fun showPermissionAlert(){
        AlertDialog.Builder(this)
            .setTitle("Bluetooth permission needed")
            .setMessage("This app needs access to nearby devices.Please grand the permission from settings")
            .setPositiveButton("settings") { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                intent.data = Uri.fromParts("package", packageName, "permissions")
                startActivity(intent)
            }
            .setNegativeButton("Cancel") { _, _ ->
                    AlertDialog.Builder(this).setTitle("App cannot run without the permission .Do you want to exit the application ?")
                        .setPositiveButton("yes"){_,_->
                            finish()
                        }
                        .setNegativeButton("No") { _, _ ->
                            showPermissionAlert()

                        }
                        .create()
                        .show()
            }
            .create()
            .show()
    }



    private val resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult())
        { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val intent = Intent(this,Connect::class.java)
                startActivity(intent);

            } else {
                showTurnONAlertDialog()
            }
        }



    fun AskUserToTurnOn() {

            val bluetoothManager: BluetoothManager = getSystemService(BluetoothManager::class.java)
            val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter
            if (bluetoothAdapter != null) {
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                if (!bluetoothAdapter.isEnabled) {
                    try{
                        resultLauncher.launch(enableBtIntent)
                    }
                    catch (e :java.lang.Exception){
                        showPermissionAlert()
                    }
                }else{
                    val intent = Intent(this,Connect::class.java)
                    startActivity(intent);
                }
            }




    }















    @Composable
    fun ButtonBox(txt:String){
        var isButtonClicked by remember { mutableStateOf(false) }
        Box(
            Modifier
                .width(100.dp)
                .height(100.dp)
                .padding(10.dp)
                .background(if (isButtonClicked) Color(0xFFFFA500) else Color.Green)
                .padding(10.dp)
                .clickable(onClick = {
                    isButtonClicked = !isButtonClicked
                    checkBluetoothPermission()
                }),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ){
            Text(text = txt, textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxSize())
        }
    }

    @Composable
    fun GridBox(row : Int=5,col:Int=5){
        Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.Center) {
            repeat(row) { row->
                Column(Modifier.fillMaxHeight(), verticalArrangement = Arrangement.Center) {
                    repeat(col) { col ->
                        ButtonBox("$row $col")
                    }
                }
            }
        }
    }

}
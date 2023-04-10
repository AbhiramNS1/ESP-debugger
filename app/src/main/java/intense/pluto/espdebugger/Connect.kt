package intense.pluto.espdebugger

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.io.IOException
import java.util.*

class Connect: ComponentActivity() {

    private var reciver:BroadcastReceiver?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            Column(
                Modifier.padding(10.dp)
            ) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.Center
                )
                {
                    Text(
                        "Bluetooth Devices",
                        style = TextStyle(
                            textAlign = TextAlign.Center,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black
                        )
                    )
                }

                Text(
                    "Paired devices",
                    modifier = Modifier.padding(15.dp),
                    fontWeight = FontWeight.Bold
                )

                PairedDeviceList()

                Text(
                    "Available devices",
                    modifier = Modifier.padding(15.dp),
                    fontWeight = FontWeight.Bold
                )

                AvilableDevicesList()

            }
        }
    }

    @SuppressLint("MissingPermission")
    @Composable
    fun PairedDeviceList() {
        val devices = remember { mutableStateListOf<BluetoothDevice>() }
        LaunchedEffect(key1 = true) {
            val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            val pairedDevices: Set<BluetoothDevice> = bluetoothManager.adapter.bondedDevices
            devices.addAll(pairedDevices)
        }
        LazyColumn {
            itemsIndexed(devices) { _, device ->
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(3.dp)
                        .border(BorderStroke(1.dp, Color.Black), shape = RoundedCornerShape(8.dp))
                        .padding(16.dp)
                ) {

                    Text(
                        text = "${device.name}  (${device.address})",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )

                }
            }
        }

    }

    @SuppressLint("MissingPermission")
    @Composable
    fun AvilableDevicesList() {

        val devices = remember { mutableStateListOf<BluetoothDevice>() }
        val context = LocalContext.current
        LaunchedEffect(key1 = Unit) {

            reciver = object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    when (intent.action) {
                        BluetoothDevice.ACTION_FOUND -> {
                            val device: BluetoothDevice? = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                            if (device != null) {
                                devices.add(device)
                            }
                        }
                    }
                }
            }


            val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            if (bluetoothManager.adapter.isEnabled) {
                val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
                context.registerReceiver(reciver, filter)
                bluetoothManager.adapter.startDiscovery()
            }
        }

        if (devices.isEmpty()) {
            Box(
                Modifier.fillMaxSize(),
            )
            CircularProgressIndicator()
        } else {
            LazyColumn {
                itemsIndexed(devices) { _, device ->
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .padding(3.dp)
                            .border(
                                BorderStroke(1.dp, Color.Black),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(16.dp)
                            .clickable{
                                if(device.bondState==BluetoothDevice.BOND_NONE) {
                                    unregisterReceiver(reciver)
                                    device.createBond()
                                }
                            }
                    ) {

                        Text(
                            text = "${device.name}  (${device.address})",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )

                    }
                }
            }
        }
    }


    @SuppressLint("MissingPermission")
    private fun connectToDevice(device: BluetoothDevice) {
        val uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB") // Standard SerialPortService ID

        Thread {
            val socket = device.createRfcommSocketToServiceRecord(uuid)

            // Cancel discovery because it will slow down the connection
            //  unregisterReceiver(receiver)
            val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
            bluetoothAdapter?.cancelDiscovery()

            // Connect to the remote device
            try {
                socket.connect()
                repeat(10){
                    Log.d("----------------------------------","Ecoonnected tooooooo theee socket RR")
                }
                 //  socket.outputStream.write(9)
                //  Do something with the connected socket, e.g. transfer data
            } catch (e: IOException) {
                // Unable to connect; close the socket and return
                try {
                    repeat(10){
                        Log.d("----------------------------------","NOOOO    RRR")
                    }
                    socket.close()
                } catch (closeException: IOException) {

                   repeat(10){
                       Log.d("----------------------------------","EROOOOOORRR")
                   }


                     }

            }
        }.start()


    }









    override fun onDestroy() {

        if(reciver!=null)
            unregisterReceiver(reciver)
        super.onDestroy()

    }


}
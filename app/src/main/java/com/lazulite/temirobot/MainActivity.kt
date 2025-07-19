package com.lazulite.temirobot

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.MediaController
import android.widget.VideoView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.DrawableRes
import androidx.annotation.UiThread
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.lazulite.temirobot.ui.theme.TemirobotTheme
import com.robotemi.sdk.NlpResult
import com.robotemi.sdk.Robot
import com.robotemi.sdk.TtsRequest
import com.robotemi.sdk.constants.HomeScreenMode
import com.robotemi.sdk.listeners.OnBeWithMeStatusChangedListener
import com.robotemi.sdk.listeners.OnGoToLocationStatusChangedListener
import com.robotemi.sdk.listeners.OnLocationsUpdatedListener
import com.robotemi.sdk.listeners.OnRobotReadyListener
import com.robotemi.sdk.permission.OnRequestPermissionResultListener
import com.robotemi.sdk.permission.Permission
import com.robotemi.sdk.voice.WakeupOrigin
import kotlinx.coroutines.flow.MutableStateFlow

class MainActivity : ComponentActivity(),OnRobotReadyListener,Robot.NlpListener,
    OnBeWithMeStatusChangedListener,OnGoToLocationStatusChangedListener,
    Robot.ConversationViewAttachesListener,Robot.WakeupWordListener,
    Robot.TtsListener,OnLocationsUpdatedListener,OnRequestPermissionResultListener {

    private var robotStatus = MutableStateFlow("Robot ready");
    private var robotFollow = MutableStateFlow("Robot Follow");
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TemirobotTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Black,
                    contentColor = Color.Blue
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        LoopingVideoPlayerWithoutControls() // Background Video
                        EventMapScreen()                    // UI on top
                    }
                }
            }
        }

        Robot.getInstance().addOnRobotReadyListener(this);
        Robot.getInstance().addNlpListener(this);
        Robot.getInstance().addOnBeWithMeStatusChangedListener(this);
        Robot.getInstance().addOnGoToLocationStatusChangedListener(this);
        Robot.getInstance().addConversationViewAttachesListenerListener(this);
        Robot.getInstance().addWakeupWordListener(this);
        Robot.getInstance().addTtsListener(this);
        Robot.getInstance().addOnLocationsUpdatedListener(this);

    }

    override fun onRobotReady(isReady: Boolean) {
        robotStatus.value = "Robot ready = $isReady";
        if(isReady){
            val locations = Robot.getInstance().locations
            Log.d("MYLOCATIONS",locations.count().toString());
            for (location in locations){
                Log.d("MYLOCATIONS",location)
            }
            Robot.getInstance().setKioskModeOn(true, mode = HomeScreenMode.DEFAULT)
            Robot.getInstance().requestToBeKioskApp() // cannot remove this line, otherwise the app will not be able to set kiosk mode, and the mic won't works
            if (Robot.getInstance().checkSelfPermission(Permission.SETTINGS) != Permission.GRANTED) {
                Robot.getInstance().requestPermissions(listOf(Permission.SETTINGS), 0)
            }
        }
    }

    @Composable
    fun Greeting(name: String, modifier: Modifier = Modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Divider(color = Color.Black, thickness = 60.dp)
            Button(
                onClick = {
                          Robot.getInstance().goTo("home base")
                },
                modifier = Modifier.size(200.dp, 50.dp)
            ) {
                Text(text = "GO HOME")
            }
            Divider(color = Color.Transparent, thickness = 10.dp)
            Button(
                onClick = {
                    Robot.getInstance().goTo("anwar")
                },
                modifier = Modifier.size(200.dp, 50.dp)
            ) {
                Text(text = "Anwar")
            }
            Divider(color = Color.Transparent, thickness = 10.dp)
            Button(
                onClick = {
                    Robot.getInstance().goTo("manikandan")
                },
                modifier = Modifier.size(200.dp, 50.dp)
            ) {
                Text(text = "Manikandan")
            }
            Divider(color = Color.Transparent, thickness = 10.dp)
            Button(
                onClick = {
                    Robot.getInstance().beWithMe()
                },
                modifier = Modifier.size(200.dp, 50.dp)
            ) {
                Text(text = "Follow Me")
            }
            Divider(color = Color.Transparent, thickness = 10.dp)
            Button(
                onClick = {
                    Robot.getInstance().stopMovement()
                },
                modifier = Modifier.size(200.dp, 50.dp)
            ) {
                Text(text = "Stop Following")
            }
            Divider(color = Color.Transparent, thickness = 10.dp)
            val statusText by robotStatus.collectAsState()
            Text(text = statusText)
            Divider(color = Color.Transparent, thickness = 10.dp)
            val beWithMe by robotFollow.collectAsState()
            Text(text = beWithMe)
        }
    }

    @Composable
    fun LoopingVideoPlayerWithoutControls() {
        val context = LocalContext.current

        AndroidView(
            factory = {
                val videoView = VideoView(it)

                val uri = Uri.parse("android.resource://${context.packageName}/raw/bg")
                videoView.setVideoURI(uri)

                videoView.setOnPreparedListener { mediaPlayer ->
                    mediaPlayer.isLooping = true
                    videoView.start()
                }

                videoView
            },
            modifier = Modifier.fillMaxSize()
        )
    }

    @Composable
    fun EventMapScreen() {
        Row(modifier = Modifier.fillMaxSize()) {
            // 🟦 Left Side: Buttons
            LeftPanel(modifier = Modifier.weight(0.3f))

            // 🗺️ Right Side: Transparent Map on Video Background
            RightPanel(modifier = Modifier.weight(0.65f))
        }
    }

    @Composable
    fun LeftPanel(modifier: Modifier = Modifier) {
        Column(
            modifier = modifier
                .fillMaxHeight()
                .background(Color(0xFF012D8C).copy(alpha = 0f))
                .padding(40.dp, 0.dp, 0.dp, 0.dp), // Optional: horizontal padding only
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            val selectedTitle = remember { mutableStateOf("") }
            Column {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.lazulite_logo),
                        contentDescription = "Logo",
                        modifier = Modifier
                            .height(70.dp)
                            .padding(40.dp, 0.dp, 0.dp)
                    )
                }

                val buttonItems = listOf(
                    ButtonItem("ROBOTIC AREA", R.drawable.btn_robotic),
                    ButtonItem("TECH EVENTS AREA", R.drawable.btn_tech_events),
                    ButtonItem("OIL & GAS EVENTS AREA", R.drawable.btn_oil_gas),
                    ButtonItem("COMMON AREA", R.drawable.btn_common_area),
                    ButtonItem("LAUNCH AREA", R.drawable.btn_launch),
                    ButtonItem("REAL ESTATE EVENTS AREA", R.drawable.btn_real_estate),
                    ButtonItem("PHOTO OPS", R.drawable.btn_photo_ops),
                    ButtonItem("GAMING ZONE", R.drawable.btn_gaming_zone),
                    ButtonItem("NEW TECHNOLOGIES", R.drawable.btn_new_technologies)
                )

                buttonItems.forEach { imageRes ->
                    ImageButton(imageRes.imageRes) {
                        Log.d("TEMI_LOCATION",imageRes.title)
                        selectedTitle.value = imageRes.title
                    }
                    Spacer(modifier = Modifier.height(0.dp)) // space between buttons
                }
            }
            ImageButton(R.drawable.btn_show_path) {
                Log.d("TEMI_LOCATION", "Navigating to: ${selectedTitle.value}")
                Robot.getInstance().goTo(selectedTitle.value)
            }
        }
    }

    @Composable
    fun ImageButton(@DrawableRes imageResId: Int, onClick: () -> Unit) {
        Image(
            painter = painterResource(id = imageResId),
            contentDescription = null,
            modifier = Modifier
                .clickable { onClick() },
            contentScale = ContentScale.None
        )
    }

    @Composable
    fun RightPanel(modifier: Modifier = Modifier) {
        Box(
            modifier = modifier
                .fillMaxHeight()
                .padding(0.dp,0.dp,0.dp,0.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.event_map), // your map image
                contentDescription = "Event Map Overlay",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }
    }

    override fun onNlpCompleted(nlpResult: NlpResult) {

    }

    override fun onBeWithMeStatusChanged(status: String) {
        robotFollow.value = "Robot Follow = $status"
    }

    override fun onConversationAttaches(isAttached: Boolean) {
        TODO("Not yet implemented")
    }

    override fun onTtsStatusChanged(ttsRequest: TtsRequest) {
        TODO("Not yet implemented")
    }

    override fun onWakeupWord(wakeupWord: String, direction: Int, origin: WakeupOrigin) {
        TODO("Not yet implemented")
    }

    override fun onGoToLocationStatusChanged(
        location: String,
        status: String,
        descriptionId: Int,
        description: String
    ) {

    }

    override fun onLocationsUpdated(locations: List<String>) {

    }

    override fun onRequestPermissionResult(
        permission: Permission,
        grantResult: Int,
        requestCode: Int
    ) {
        Log.d("MainActivity", "onRequestPermissionResult: $permission, $grantResult, $requestCode")

        if(grantResult != Permission.GRANTED){
            Robot.getInstance().requestPermissions(listOf(Permission.SETTINGS), 0)
        }
    }
}

data class ButtonItem(
    val title: String,
    @DrawableRes val imageRes: Int
)
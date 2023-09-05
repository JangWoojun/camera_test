package com.woojun.camera_test

import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import com.otaliastudios.cameraview.CameraListener
import com.otaliastudios.cameraview.PictureResult
import com.otaliastudios.cameraview.VideoResult
import com.otaliastudios.cameraview.controls.Mode
import com.woojun.camera_test.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.camera.setLifecycleOwner(this)
        binding.apply {
            // 사진 또는 동영상 결과 나오는 리스너
            camera.addCameraListener(object : CameraListener() {
                // 사진 촬영 결과 리스너
                override fun onPictureTaken(result: PictureResult) {
                    // 이미지를 비트맵으로 변환
                    result.toBitmap { bitmap ->
                        if (bitmap != null) {

                            // 비트맵을 파일로 저장 (백그라운드 스레드에서 수행)
                            CoroutineScope(Dispatchers.IO).launch {
                                val file = File(
                                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                                    "picture_${System.currentTimeMillis()}.jpg"
                                )

                                try {
                                    withContext(Dispatchers.IO) {
                                        val fileOutputStream = FileOutputStream(file)
                                        // 비트맵을 JPEG 형식으로 저장
                                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream)
                                        fileOutputStream.close()
                                    }

                                    // 파일 저장이 완료되면 어떤 작업을 수행할 수 있습니다.
                                    // 예를 들어, 사용자에게 성공적으로 저장되었다는 메시지를 표시할 수 있습니다.
                                } catch (e: IOException) {
                                    e.printStackTrace()
                                    // 파일 저장 중 오류 발생 시 처리할 코드를 추가할 수 있습니다.
                                }
                            }
                        }
                    }
                }

                // 동영상 촬영 결과 리스너
                override fun onVideoTaken(result: VideoResult) {
                    result.file.apply {
                        // 동영상 파일 사용
                    }
                }

                // 동영상 촬영 종료 리스너
                override fun onVideoRecordingEnd() {
                    super.onVideoRecordingEnd()
                }

                // 사진 촬영 시작 리스너
                override fun onPictureShutter() {
                }

                // 동영상 촬영 시작 리스너
                override fun onVideoRecordingStart() {
                }
            })

            //region 카메라 관련 버튼 리스너
            // 전,후면 카메라 전환 버튼 리스너
            switchBtn.setOnClickListener {
                camera.toggleFacing()
            }
            // 사진 또는 동영상 촬영 버튼 리스너
            captureBtn.setOnClickListener {
                // 카메라 촬영중이라면 촬영종료
                if (camera.isTakingVideo) {
                    camera.stopVideo()
                }
                // 카메라 모드에 따라 다르게 실행
                else if (camera.mode == Mode.PICTURE) camera.takePicture()
                else if (camera.mode == Mode.VIDEO) camera.takeVideo(
                    File(this@MainActivity.filesDir, "video_${System.currentTimeMillis()}.mp4")
                )
            }

    }
}
}
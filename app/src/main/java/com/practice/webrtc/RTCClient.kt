package com.practice.webrtc

import android.app.Application
import android.content.Context
import org.webrtc.*
import java.lang.IllegalStateException

/**

 * Created by juhyang on 2021/07/30.

 */
class RTCClient(context : Application, private val localVideoOutput : SurfaceViewRenderer) {

    companion object {
        private const val LOCAL_TRACK_ID = "local_track"
    }

    private val rootEglBase : EglBase = EglBase.create()

    init {
        initPeerConnectionFactory(context)
        initSurfaceView(localVideoOutput)
    }

    private val peerConnectionFactory by lazy { buildPeerConnectionFactory() }
    private val videoCapturer by lazy { getVideoCapturer(context) }
    private val localVideoSource by lazy { peerConnectionFactory.createVideoSource(false) }

    /*
    * PeerConnectionFactory 초기화
    * 먼저 백그라운드에서 발생하는 일을 추적한 다음 기본 라이브러리를 켜고자 하는 기능을 지정해야 합니다.
    * 이 경우 H264 비디오 형식을 구성합니다.
    * */
    private fun initPeerConnectionFactory (context : Application) {
        val options = PeerConnectionFactory.InitializationOptions.builder(context)
            .setEnableInternalTracer(true)
            .setFieldTrials("WebRTC-H264HighProfile/Enabled/")
            .createInitializationOptions()
        PeerConnectionFactory.initialize(options)
    }

    /*
    * PeerConnectionFactory.Builder 를 사용하여 PeerConnectionFactory 를 사용할 수 있습니다.
    * PeerConnectionFactory 를 빌드 할 때 사용중인 비디오 코덱을 지정하는 것이 중요합니다.
    * 이 샘플에서는 기본 비디오 코덱을 사용합니다. 또한 암호화를 비활성화 합니다.
    * */
    private fun buildPeerConnectionFactory() : PeerConnectionFactory {
        return PeerConnectionFactory
            .builder()
            .setVideoDecoderFactory(DefaultVideoDecoderFactory(rootEglBase.eglBaseContext))
            .setVideoEncoderFactory(DefaultVideoEncoderFactory(rootEglBase.eglBaseContext, true, true))
            .setOptions(PeerConnectionFactory.Options().apply {
                disableEncryption = true
                disableNetworkMonitor = true
            })
            .createPeerConnectionFactory()
    }

    /*
    * 비디오 소스는 단순히 카메라입니다. 기본 webRTC 라이브러리에는 Camera2Enumerator 전면 카메라를 가져오는 데 사용할 수 있는 편리한 도우미가 있습니다.
    * */
    private fun getVideoCapturer(context : Context) = Camera2Enumerator(context).run {
        deviceNames.find {
            isFrontFacing(it)
        }?.let {
            createCapturer(it, null)
        } ?: throw IllegalStateException()
    }

    /*
    * 제공하는 비디오 스트림을 미러링하고, 하드웨어 가속을 활성화 해야 합니다.
    * */
    private fun initSurfaceView(view: SurfaceViewRenderer) = view.run {
        setMirror(true)
        setEnableHardwareScaler(true)
        init(rootEglBase.eglBaseContext, null)
    }

    /*
    * 전면 카메라가 있으면 PeerConnectionFactory 및 VideoTrack 에서 VideoSource 를 만든 다음 SurfaceView 렌더러를 VideoTrack 에 연결할 수 있습니다.
    * */
    fun startLocalVideoCapture() {
        val surfaceTextureHelper = SurfaceTextureHelper.create(Thread.currentThread().name, rootEglBase.eglBaseContext)
        (videoCapturer as VideoCapturer).initialize(surfaceTextureHelper, localVideoOutput.context, localVideoSource.capturerObserver)
        videoCapturer.startCapture(320, 240, 60)
        val localVideoTrack = peerConnectionFactory.createVideoTrack(LOCAL_TRACK_ID, localVideoSource)
        localVideoTrack.addSink(localVideoOutput)
    }
}

package org.jellyfin.androidtv.danmu.ui.action;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import org.jellyfin.androidtv.R;
import org.jellyfin.androidtv.data.compat.StreamInfo;
import org.jellyfin.androidtv.ui.playback.PlaybackController;
import org.jellyfin.androidtv.ui.playback.overlay.CustomPlaybackTransportControlGlue;
import org.jellyfin.androidtv.ui.playback.overlay.VideoPlayerAdapter;
import org.jellyfin.androidtv.ui.playback.overlay.action.CustomAction;
import org.jellyfin.sdk.model.api.MediaSourceInfo;
import org.jellyfin.sdk.model.api.MediaStream;
import org.jellyfin.sdk.model.api.MediaStreamType;

import java.util.List;

public class PlayInfoAction extends CustomAction {
//    private PlaybackController playbackController;
    private Context context;

    public PlayInfoAction(@NonNull Context context, @NonNull CustomPlaybackTransportControlGlue customPlaybackTransportControlGlue, PlaybackController playbackController) {
        super(context, customPlaybackTransportControlGlue);
        this.context = context;
//        playbackController.getCurrentMediaSource()
        initializeWithIcon(R.drawable.media_info);
    }

    @Override
    public void handleClickAction(@NonNull PlaybackController playbackController, @NonNull VideoPlayerAdapter videoPlayerAdapter, @NonNull Context context, @NonNull View view) {
        // {"bitrate":4196608,"container":"mov","defaultAudioStreamIndex":1,"defaultSubtitleStreamIndex":-1,"eTag":"ad9e56cf4f048aa742ea023e639bde89","formats":[],"genPtsInput":false,"id":"9d23ca269a8381e13a0af3009d1e3f69","ignoreDts":false,"ignoreIndex":false,"isInfiniteStream":false,"isRemote":false,"mediaStreams":[{"aspectRatio":"16:9","audioSpatialFormat":"NONE","averageFrameRate":25.0,"bitDepth":8,"bitRate":4031796,"codec":"h264","codecTag":"avc1","displayTitle":"1080p H264 SDR","height":1080,"index":0,"isAnamorphic":false,"isAvc":true,"isDefault":true,"isExternal":false,"isForced":false,"isHearingImpaired":false,"isInterlaced":false,"isTextSubtitleStream":false,"language":"und","level":50.0,"nalLengthSize":"4","pixelFormat":"yuv420p","profile":"High","realFrameRate":25.0,"refFrames":1,"supportsExternalStream":false,"timeBase":"1/90000","type":"VIDEO","videoRange":"UNKNOWN","videoRangeType":"UNKNOWN","width":1920},{"audioSpatialFormat":"NONE","bitRate":157588,"channelLayout":"stereo","channels":2,"codec":"aac","codecTag":"mp4a","displayTitle":"AAC - Stereo - 默认","index":1,"isAvc":false,"isDefault":true,"isExternal":false,"isForced":false,"isHearingImpaired":false,"isInterlaced":false,"isTextSubtitleStream":false,"language":"und","level":0.0,"profile":"LC","sampleRate":44100,"supportsExternalStream":false,"timeBase":"1/44100","type":"AUDIO","videoRange":"UNKNOWN","videoRangeType":"UNKNOWN"}],"name":"欢笑老弄堂 - S01E01 - 第 - 1 集","path":"/media/links/dianshiju/国产剧/欢笑老弄堂 (2024)/Season 1/欢笑老弄堂 - S01E01 - 第 - 1 集.mp4","protocol":"FILE","readAtNativeFramerate":false,"requiredHttpHeaders":{},"requiresClosing":false,"requiresLooping":false,"requiresOpening":false,"runTimeTicks":20471873020,"size":1073905393,"supportsDirectPlay":true,"supportsDirectStream":true,"supportsProbing":false,"supportsTranscoding":true,"transcodingSubProtocol":"HTTP","type":"DEFAULT","videoType":"VIDEO_FILE"}
        LinearLayout playbackInfoView = view.getRootView().findViewById(R.id.playback_info);

        if (playbackInfoView == null) {
            return;
        }
        playbackInfoView.removeAllViews();
//        MediaSourceInfo currentMediaSource = playbackController.getCurrentMediaSource();

        // {"Container":"mov","Context":"Streaming","DeviceProfile":{"allSupportedAudioCodecs":["aac","mp3","mp2","aac_latm","alac","ac3","eac3","dca","dts","mlp","truehd","pcm_alaw","pcm_mulaw","pcm_s16le","pcm_s20le","pcm_s24le","opus","flac","vorbis"],"allSupportedAudioCodecsWithoutFFmpegExperimental":["aac","mp3","mp2","aac_latm","alac","ac3","eac3","dts","mlp","pcm_alaw","pcm_mulaw","pcm_s16le","pcm_s20le","pcm_s24le","opus","flac","vorbis"],"downmixSupportedAudioCodecs":["aac","mp3","mp2"],"CodecProfiles":[{"ApplyConditions":[],"Codec":"h264","Conditions":[{"Condition":"EqualsAny","IsRequired":false,"Property":"VideoProfile","Value":"high|main|baseline|constrained baseline"},{"Condition":"LessThanEqual","IsRequired":false,"Property":"VideoLevel","Value":"51"}],"Type":"Video"},{"ApplyConditions":[{"Condition":"GreaterThanEqual","IsRequired":false,"Property":"Width","Value":"1200"}],"Codec":"h264","Conditions":[{"Condition":"LessThanEqual","IsRequired":false,"Property":"RefFrames","Value":"12"}],"Type":"Video"},{"ApplyConditions":[{"Condition":"GreaterThanEqual","IsRequired":false,"Property":"Width","Value":"1900"}],"Codec":"h264","Conditions":[{"Condition":"LessThanEqual","IsRequired":false,"Property":"RefFrames","Value":"4"}],"Type":"Video"},{"ApplyConditions":[],"Codec":"hevc","Conditions":[{"Condition":"NotEquals","IsRequired":false,"Property":"VideoProfile","Value":"Main 10"}],"Type":"Video"},{"ApplyConditions":[{"Condition":"Equals","IsRequired":false,"Property":"VideoProfile","Value":"Main"}],"Codec":"hevc","Conditions":[{"Condition":"LessThanEqual","IsRequired":false,"Property":"VideoLevel","Value":"156"}],"Type":"Video"},{"ApplyConditions":[],"Codec":"av1","Conditions":[{"Condition":"NotEquals","IsRequired":false,"Property":"VideoProfile","Value":"none"}],"Type":"Video"},{"ApplyConditions":[],"Conditions":[{"Condition":"LessThanEqual","IsRequired":false,"Property":"AudioChannels","Value":"2"}],"Type":"VideoAudio"}],"ContainerProfiles":[],"DirectPlayProfiles":[{"AudioCodec":"aac,mp3,mp2","Container":"m4v,mov,xvid,vob,mkv,wmv,asf,ogm,ogv,mp4,webm,ts,hls","Type":"Video","VideoCodec":"h264,hevc,vp8,vp9,mpeg,mpeg2video,av1"},{"Container":"aac,mp3,mp2,aac_latm,alac,ac3,eac3,dca,dts,mlp,truehd,pcm_alaw,pcm_mulaw,pcm_s16le,pcm_s20le,pcm_s24le,opus,flac,vorbis,mpa,wav,wma,ogg,oga,webma,ape","Type":"Audio"},{"Container":"jpg,jpeg,png,gif,webp","Type":"Photo"}],"EnableAlbumArtInDidl":false,"EnableMSMediaReceiverRegistrar":false,"EnableSingleAlbumArtLimit":false,"EnableSingleSubtitleLimit":false,"IgnoreTranscodeByteRangeRequests":false,"MaxAlbumArtHeight":0,"MaxAlbumArtWidth":0,"MaxStaticBitrate":100000000,"MaxStreamingBitrate":20000000,"MusicStreamingTranscodingBitrate":128000,"Name":"AndroidTV-ExoPlayer","ProfileType":"System","RequiresPlainFolders":false,"RequiresPlainVideoItems":false,"ResponseProfiles":[],"SubtitleProfiles":[{"Format":"srt","Method":"External"},{"Format":"subrip","Method":"External"},{"Format":"ass","Method":"Encode"},{"Format":"ssa","Method":"Encode"},{"Format":"pgs","Method":"Embed"},{"Format":"pgssub","Method":"Embed"},{"Format":"dvbsub","Method":"Embed"},{"Format":"dvdsub","Method":"Encode"},{"Format":"vtt","Method":"Embed"},{"Format":"sub","Method":"Embed"},{"Format":"idx","Method":"Embed"}],"SupportedMediaTypes":"Audio,Photo,Video","TimelineOffsetSeconds":0,"TranscodingProfiles":[{"AudioCodec":"aac,mp3,mp2","BreakOnNonKeyFrames":false,"Container":"ts","Context":"Streaming","CopyTimestamps":false,"EnableMpegtsM2TsMode":false,"EnableSubtitlesInManifest":false,"EstimateContentLength":false,"MinSegments":0,"Protocol":"hls","SegmentLength":0,"TranscodeSeekInfo":"Auto","Type":"Video","VideoCodec":"hevc,h264"},{"AudioCodec":"mp3","BreakOnNonKeyFrames":false,"Container":"mp3","Context":"Streaming","CopyTimestamps":false,"EnableMpegtsM2TsMode":false,"EnableSubtitlesInManifest":false,"EstimateContentLength":false,"MinSegments":0,"SegmentLength":0,"TranscodeSeekInfo":"Auto","Type":"Audio"}],"XmlRootAttributes":[]},"ItemId":"9d23ca26-9a83-81e1-3a0a-f3009d1e3f69","MediaSource":{"bitrate":4196608,"container":"mov","defaultAudioStreamIndex":1,"defaultSubtitleStreamIndex":-1,"eTag":"ad9e56cf4f048aa742ea023e639bde89","formats":[],"genPtsInput":false,"id":"9d23ca269a8381e13a0af3009d1e3f69","ignoreDts":false,"ignoreIndex":false,"isInfiniteStream":false,"isRemote":false,"mediaStreams":[{"aspectRatio":"16:9","audioSpatialFormat":"NONE","averageFrameRate":25.0,"bitDepth":8,"bitRate":4031796,"codec":"h264","codecTag":"avc1","displayTitle":"1080p H264 SDR","height":1080,"index":0,"isAnamorphic":false,"isAvc":true,"isDefault":true,"isExternal":false,"isForced":false,"isHearingImpaired":false,"isInterlaced":false,"isTextSubtitleStream":false,"language":"und","level":50.0,"nalLengthSize":"4","pixelFormat":"yuv420p","profile":"High","realFrameRate":25.0,"refFrames":1,"supportsExternalStream":false,"timeBase":"1/90000","type":"VIDEO","videoRange":"UNKNOWN","videoRangeType":"UNKNOWN","width":1920},{"audioSpatialFormat":"NONE","bitRate":157588,"channelLayout":"stereo","channels":2,"codec":"aac","codecTag":"mp4a","displayTitle":"AAC - Stereo - 默认","index":1,"isAvc":false,"isDefault":true,"isExternal":false,"isForced":false,"isHearingImpaired":false,"isInterlaced":false,"isTextSubtitleStream":false,"language":"und","level":0.0,"profile":"LC","sampleRate":44100,"supportsExternalStream":false,"timeBase":"1/44100","type":"AUDIO","videoRange":"UNKNOWN","videoRangeType":"UNKNOWN"}],"name":"欢笑老弄堂 - S01E01 - 第 - 1 集","path":"/media/links/dianshiju/国产剧/欢笑老弄堂 (2024)/Season 1/欢笑老弄堂 - S01E01 - 第 - 1 集.mp4","protocol":"FILE","readAtNativeFramerate":false,"requiredHttpHeaders":{},"requiresClosing":false,"requiresLooping":false,"requiresOpening":false,"runTimeTicks":20471873020,"size":1073905393,"supportsDirectPlay":true,"supportsDirectStream":true,"supportsProbing":false,"supportsTranscoding":true,"transcodingSubProtocol":"HTTP","type":"DEFAULT","videoType":"VIDEO_FILE"},"MediaUrl":"https://jellyfin.fengymi.top:9443/Videos/9d23ca26-9a83-81e1-3a0a-f3009d1e3f69/stream.mov?api_key\u003dfe7c6897aa4542d381f3cae0ffcf9170\u0026DeviceId\u003d429dc5a892cb31770873171895d3cfdb0c03b94b\u0026MediaSourceId\u003d9d23ca269a8381e13a0af3009d1e3f69\u0026Tag\u003dad9e56cf4f048aa742ea023e639bde89\u0026Static\u003dtrue","PlayMethod":"DirectPlay","PlaySessionId":"d0e9a4e271744acc8a5444c14a668718","RunTimeTicks":20471873020,"StartPositionTicks":0,"SubtitleDeliveryMethod":"Encode","TranscodeSeekInfo":"Auto"}
        StreamInfo currentStreamInfo = playbackController.getCurrentStreamInfo();
        List<MediaStream> mediaStreams = currentStreamInfo.getMediaSource().getMediaStreams();
        for (MediaStream mediaStream : mediaStreams) {
            MediaStreamType type = mediaStream.getType();
            if (MediaStreamType.VIDEO.equals(type)) {
                // {"aspectRatio":"16:9","audioSpatialFormat":"NONE","averageFrameRate":25,"bitDepth":8,"bitRate":4031796,"codec":"h264","codecTag":"avc1","displayTitle":"1080p H264 SDR","height":1080,"index":0,"isAnamorphic":false,"isAvc":true,"isDefault":true,"isExternal":false,"isForced":false,"isHearingImpaired":false,"isInterlaced":false,"isTextSubtitleStream":false,"language":"und","level":50,"nalLengthSize":"4","pixelFormat":"yuv420p","profile":"High","realFrameRate":25,"refFrames":1,"supportsExternalStream":false,"timeBase":"1/90000","type":"VIDEO","videoRange":"UNKNOWN","videoRangeType":"UNKNOWN","width":1920}
                Integer height = mediaStream.getHeight();
                Integer width = mediaStream.getWidth();
                String displayTitle = mediaStream.getDisplayTitle(); // 1080p H264 SDR
                String codec = mediaStream.getCodec(); // h264
                Float averageFrameRate = mediaStream.getAverageFrameRate(); // 25

                playbackInfoView.addView(getVideoInfo(mediaStream));
            }
            if (MediaStreamType.AUDIO.equals(type)) {
                // {"audioSpatialFormat":"NONE","bitRate":157588,"channelLayout":"stereo","channels":2,"codec":"aac","codecTag":"mp4a","displayTitle":"AAC - Stereo - 默认","index":1,"isAvc":false,"isDefault":true,"isExternal":false,"isForced":false,"isHearingImpaired":false,"isInterlaced":false,"isTextSubtitleStream":false,"language":"und","level":0,"profile":"LC","sampleRate":44100,"supportsExternalStream":false,"timeBase":"1/44100","type":"AUDIO","videoRange":"UNKNOWN","videoRangeType":"UNKNOWN"}
            }
        }
        playbackInfoView.setVisibility(View.VISIBLE);

//        GsonJsonSerializer gsonJsonSerializer = new GsonJsonSerializer();
//        Timber.d("currentMediaSource: %s", gsonJsonSerializer.SerializeToString(currentMediaSource));
//        Timber.d("currentStreamInfo: %s", gsonJsonSerializer.SerializeToString(currentStreamInfo));
    }

    public void createStats(MediaSourceInfo currentMediaSource, StreamInfo currentStreamInfo, @NonNull View view, PlaybackController playbackController) {

//        MediaStream mediaStream;
        LinearLayout playbackInfo = view.findViewById(R.id.playback_info);
        LinearLayout streamingInfo = view.findViewById(R.id.streaming_info);
        LinearLayout originalInfo = view.findViewById(R.id.original_info);

//        playbackInfo.removeAllViews();
//        if (playbackController.isNativeMode()) {
//            str = "ExoPlayer";
//        } else {
//            str = "MPV";
//        }
//
//        playbackInfo.addView((View)getStatLine(2131887656, str));
//        if (Utils.isShield())
//            playbackInfo.addView((View)getStatLine(2131887294, DisplayHelper.getCurrentDisplayModeName()));
//        String str = DisplayHelper.getHdrSupportString();
//        if (!"".equals(str))
//            playbackInfo.addView((View)getStatLine(2131887372, str));
//
//        PlayMethod playMethod = currentStreamInfo.getPlayMethod();
//        if (playMethod != null) {
//            if (PlayMethod.DirectStream.equals(playMethod)) {
//                str = "Direct Stream";
//            } else if (PlayMethod.Transcode.equals(playMethod)){
//                str = "Transcode";
//            }
//        } else {
//            str = "Direct Play";
//        }
//
//        playbackInfo.addView((View)getStatLine(2131887655, str));
//        if (encoderProtocol != null) {
//            encoderProtocol.getSerialName()
//            str = "HLS";
//        } else if (this.mPlaybackController.isDirectAccess()) {
//            str = "File";
//        } else if (this.mApplication.getApiClient().getApiUrl().startsWith("https")) {
//            str = "HTTPS";
//        } else {
//            str = "HTTP";
//        }
//        playbackInfo.addView((View)getStatLine(2131887658, str));
//        if (currentStreamInfo.getAudioStreamIndex() != null) {
//            mediaStream = currentMediaSource.getMediaStreams().get(currentStreamInfo.getAudioStreamIndex().intValue());
//        } else {
//            mediaStream = currentMediaSource.getDefaultAudioStream();
//        }
//        streamingInfo.removeAllViews();
//        str = " (direct)";
//        if (paramTranscodingInfo != null) {
//            String str1;
//            StringBuilder stringBuilder2 = new StringBuilder();
//            stringBuilder2.append(paramTranscodingInfo.getWidth());
//            stringBuilder2.append("x");
//            stringBuilder2.append(paramTranscodingInfo.getHeight());
//            streamingInfo.addView((View)getStatLine(2131887663, stringBuilder2.toString()));
//            StringBuilder stringBuilder3 = new StringBuilder();
//            stringBuilder3.append(Utils.getFriendlyCodec(paramTranscodingInfo.getVideoCodec(), ""));
//            if (paramTranscodingInfo.getIsVideoDirect()) {
//                str1 = " (direct)";
//            } else {
//                str1 = "";
//            }
//            stringBuilder3.append(str1);
//            streamingInfo.addView((View)getStatLine(2131887662, stringBuilder3.toString()));
//            String str2 = paramTranscodingInfo.getAudioCodec();
//            StringBuilder stringBuilder1 = new StringBuilder();
//            stringBuilder1.append("");
//            if (!paramTranscodingInfo.getIsAudioDirect())
//                str = "";
//            stringBuilder1.append(str);
//            streamingInfo.addView((View)getStatLine(2131887649, Utils.getFriendlyCodec(str2, stringBuilder1.toString())));
//            streamingInfo.addView((View)getStatLine(2131887648, Utils.getAudioChannelsString(paramTranscodingInfo.getAudioChannels().intValue())));
//            streamingInfo.addView((View)getStatLine(2131887650, Utils.getFriendlyBitrate(paramTranscodingInfo.getBitrate())));
//            streamingInfo.addView((View)getStatLine(2131887429, this.mPlaybackController.getCurrentMaxBitrateStr()));
//            if (paramTranscodingInfo.getFramerate() != null)
//                streamingInfo.addView((View)getStatLine(2131887652, String.format("%.2f fps", new Object[] { paramTranscodingInfo.getFramerate() })));
//            if (this.mPlaybackController.getTranscodeReasons().size() > 0)
//                streamingInfo.addView((View)getStatLine(2131887262, getString(((Integer)this.mPlaybackController.getTranscodeReasons().get(0)).intValue())));
//            if (paramTranscodingInfo.getCompletionPercentage() != null)
//                streamingInfo.addView((View)getStatLine(2131887660, String.format("%.2f%%", new Object[] { paramTranscodingInfo.getCompletionPercentage() })));
//            if (this.mPlaybackController.isBurningSubs())
//                streamingInfo.addView((View)getStatLine(2131887239, ""));
//        } else {
//            currentMediaSource.getVideo3dFormat()
//            if (currentMediaSource.getVideoStream() != null) {
//                StringBuilder stringBuilder = new StringBuilder();
//                stringBuilder.append(currentMediaSource.getVideoStream().getWidth());
//                stringBuilder.append("x");
//                stringBuilder.append(currentMediaSource.getVideoStream().getHeight());
//                streamingInfo.addView((View)getStatLine(2131887663, stringBuilder.toString()));
//                stringBuilder = new StringBuilder();
//                stringBuilder.append(Utils.getFriendlyCodec(currentMediaSource.getVideoStream().getCodec(), ""));
//                stringBuilder.append(" (direct)");
//                streamingInfo.addView((View)getStatLine(2131887662, stringBuilder.toString()));
//                streamingInfo.addView((View)getStatLine(2131887650, Utils.getFriendlyBitrate(currentMediaSource.getVideoStream().getBitRate())));
//            }
//            if (mediaStream != null) {
//                StringBuilder stringBuilder = new StringBuilder();
//                stringBuilder.append(Utils.getFriendlyCodec(mediaStream.getCodec(), mediaStream.getProfile()));
//                if (this.mPlaybackController.isUsingFfmpegForAudio())
//                    str = " (software decode)";
//                stringBuilder.append(str);
//                streamingInfo.addView((View)getStatLine(2131887649, stringBuilder.toString()));
//            }
//        }
//        originalInfo.removeAllViews();
//        originalInfo.addView((View)getStatLine(2131887651, currentMediaSource.getContainer()));
//        if (currentMediaSource.getVideoStream() != null) {
//            originalInfo.addView((View)getStatLine(2131887662, Utils.getFriendlyCodec(currentMediaSource.getVideoStream().getCodec(), "")));
//            if (currentMediaSource.getVideoStream().getBitRate() != null)
//                originalInfo.addView((View)getStatLine(2131887661, Utils.getFriendlyBitrate(currentMediaSource.getVideoStream().getBitRate())));
//            originalInfo.addView((View)getStatLine(2131887654, String.format("%.2f fps", new Object[] { currentMediaSource.getVideoStream().getAverageFrameRate() })));
//        }
//        if (mediaStream != null) {
//            String str1;
//            originalInfo.addView((View)getStatLine(2131887649, Utils.getFriendlyCodec(mediaStream.getCodec(), mediaStream.getProfile())));
//            if (mediaStream.getBitRate() != null)
//                originalInfo.addView((View)getStatLine(2131887647, Utils.getFriendlyBitrate(mediaStream.getBitRate())));
//            if (mediaStream.getChannelLayout() != null) {
//                str1 = mediaStream.getChannelLayout();
//            } else if (mediaStream.getChannels() != null) {
//                str1 = Utils.getAudioChannelsString(mediaStream.getChannels().intValue());
//            } else {
//                str1 = getString(2131887235);
//            }
//            originalInfo.addView((View)getStatLine(2131887648, str1));
//        }
    }

    private LinearLayout getVideoInfo(MediaStream videoMediaStream) {
        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        TextView textView = new TextView(context);
//        textView.setTypeface(TvApp.getApplication().getDefaultFont(), 1);
        textView.setTextSize(12.0F);
        textView.setTextColor(Color.WHITE);
        textView.setText(R.string.streaming_info_title);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-2, -2);
        layoutParams.rightMargin = 50;
        textView.setLayoutParams(layoutParams);
        linearLayout.addView(textView);
        textView = new TextView(context);
//        textView.setTypeface(TvApp.getApplication().getDefaultFont());
        textView.setTextSize(12.0F);
        textView.setTextColor(-3355444);
        textView.setAlpha(0.85F);

        StringBuilder sb = new StringBuilder();
        Integer height = videoMediaStream.getHeight();
        Integer width = videoMediaStream.getWidth();
        String displayTitle = videoMediaStream.getDisplayTitle(); // 1080p H264 SDR
        String codec = videoMediaStream.getCodec(); // h264
        Float averageFrameRate = videoMediaStream.getAverageFrameRate(); // 25

        sb.append(width).append(" x ").append(height);
        sb.append("\n")
                .append("<br/>")
                .append(" displayTitle: ")
                .append(displayTitle);

        textView.setText(sb.toString());
        linearLayout.addView(textView);
        return linearLayout;
    }
}

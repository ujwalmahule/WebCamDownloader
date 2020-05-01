package WebCam.Downloader;

import java.util.Comparator;

public class VideoFileNameComparator implements Comparator<VideoFileName> {

	@Override
	public int compare(VideoFileName arg0, VideoFileName arg1) {
		return arg0.getTimeStamp().compareTo(arg1.getTimeStamp());
	}

}

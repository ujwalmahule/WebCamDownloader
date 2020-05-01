package WebCam.Downloader;

import java.io.File;
import java.util.Calendar;
import java.util.Date;

import org.json.JSONObject;

public class VideoFileName {
	
	private File file;
	private String fileName;
	private Date timeStamp;
	private long size;
	private String ext;
	
	public VideoFileName(String sourceIP, JSONObject obj) {
		String name = obj.getString("name");
		fileName = "http://" + sourceIP + "/v" + obj.getString("path") + "/" + name;
		size = obj.getLong("size");
		timeStamp = getTimeStamp(name);
		ext = name.split("\\.")[1];
	}

	public VideoFileName(File file) {
		this.file = file;
		this.fileName = file.getAbsolutePath();
		timeStamp = getTimeStamp(file.getName());
		ext = "";
	}

	public File getFile() {
		return file;
	}
	
	private Date getTimeStamp(String name) {
		String[] temp = name.split("_");
		String[] date = temp[1].split("-");
		String[] time = temp[2].split("-");
		Calendar timeStamp = Calendar.getInstance();
		timeStamp.clear();
		timeStamp.set(Integer.parseInt(date[0]), Integer.parseInt(date[1])-1, Integer.parseInt(date[2]), Integer.parseInt(time[0]), Integer.parseInt(time[1].split("\\.")[0]));
		return timeStamp.getTime();
	}

	public String getFileName() {
		return fileName;
	}
	
	public long getSize() {
		return size;
	}
	
	public Date getTimeStamp() {
		return timeStamp;
	}
	
	public String getExt() {
		return ext;
	}
	
	@Override
	public String toString() {
		return fileName;
	}

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof VideoFileName) {
			return this.fileName.equals(((VideoFileName)obj).fileName);
		}
		return super.equals(obj);
	}
	
	@Override
	public int hashCode() {
		return fileName.hashCode();
	}
}

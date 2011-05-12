package x10.runtime.bytecode;

import java.io.IOException;
import java.io.InputStream;

public class ByteArrayUtil {
	// FIXME: signed
	public static byte getByte(byte[] b, int o) {
		return (byte)(b[o] & 0xFF);
	}
	// FIXME: signed
	public static short getShort(byte[] b, int o) {
		return (short)(((b[o] & 0xFF) << 8) + (b[o+1] & 0xFF));
	}
	// FIXME: signed
	public static int getInt(byte[] b, int o) {
		return ((b[o] & 0xFF) << 24) + ((b[o+1] & 0xFF) << 16) + ((b[o+2] & 0xFF) << 8) + (b[o+3] & 0xFF);
	}

	// FIXME: signed
	public static byte readByte(InputStream in) throws IOException {
		return (byte)(in.read() & 0xFF);
	}
	// FIXME: signed
	public static short readShort(InputStream in) throws IOException {
		return (short)((in.read() & 0xFF) << 8 | (in.read() & 0xFF));
	}
	// FIXME: signed
	public static int readInt(InputStream in) throws IOException {
		return (int)((in.read() & 0xFF) << 24 | (in.read() & 0xFF) << 16 |
				     (in.read() & 0xFF) << 8 | (in.read() & 0xFF));
	}
	public static int putByte(byte[] b, int o, short v) {
		b[o] = (byte)(v & 0xFF);
		return o+1;
	}
	public static int putShort(byte[] b, int o, short v) {
		b[o]   = (byte)((v >> 8) & 0xFF);
		b[o+1] = (byte)(v & 0xFF);
		return o+2;
	}
	public static int putInt(byte[] b, int o, int v) {
		b[o]   = (byte)((v >> 24) & 0xFF);
		b[o+1] = (byte)((v >> 16) & 0xFF);
		b[o+2] = (byte)((v >> 8) & 0xFF);
		b[o+3] = (byte)(v & 0xFF);
		return o+4;
	}
}
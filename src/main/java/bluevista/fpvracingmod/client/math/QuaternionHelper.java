package bluevista.fpvracingmod.client.math;

import net.minecraft.util.math.*;

public class QuaternionHelper {
	public static Quaternion rotateX(Quaternion quat, float amount) {
	    double radHalfAngle = Math.toRadians((double) amount) / 2.0;
	    Quaternion rot = new Quaternion((float) Math.sin(radHalfAngle), 0.0f, 0.0f, (float) Math.cos(radHalfAngle));
	    quat.hamiltonProduct(rot);
		return quat;
	}
	
	public static Quaternion rotateY(Quaternion quat, float amount) {
	    double radHalfAngle = Math.toRadians((double) amount) / 2.0;
	    Quaternion rot = new Quaternion(0.0f, (float) Math.sin(radHalfAngle), 0.0f, (float) Math.cos(radHalfAngle));
	    quat.hamiltonProduct(rot);
		return quat;
	}
	
	public static Quaternion rotateZ(Quaternion quat, float amount) {
	    double radHalfAngle = Math.toRadians((double) amount) / 2.0;
	    Quaternion rot = new Quaternion(0.0f, 0.0f, (float) Math.sin(radHalfAngle), (float) Math.cos(radHalfAngle));
	    quat.hamiltonProduct(rot);
	    return quat;
	}
}
package xyz.teamcatalyst.breedr;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.FirebaseUser;

/**
 * YOYO HOLDINGS
 *
 * @author A-Ar Andrew Concepcion
 * @since 23/03/2017
 */
public class FirebaseUtils {
    public static void sendVerificationEmail(FirebaseUser user, OnCompleteListener<Void> onCompleteListener) {
        if (user != null) {
            user.sendEmailVerification().addOnCompleteListener(onCompleteListener);
        }
    }
}

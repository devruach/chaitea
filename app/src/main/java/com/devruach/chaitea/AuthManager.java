package com.devruach.chaitea;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;

/**
 * Created by USER on 2017-04-02.
 */

public class AuthManager {
    private static final String TAG = "AuthManager";

    private static AuthManager sInstance = new AuthManager();

    public static AuthManager getInstance() {
        return sInstance;
    }

    private String mToken = null;

    // declare auth
    private FirebaseAuth mAuth;

    // declare auth_state_listener
    private FirebaseAuth.AuthStateListener mAuthListener;

    private AuthManager() {
    }

    public void setup() {
        // initialize auth
        mAuth = FirebaseAuth.getInstance();

        // initialize auth_state_listener
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                    user.getToken(true).addOnSuccessListener(new OnSuccessListener<GetTokenResult>() {
                        @Override
                        public void onSuccess(GetTokenResult getTokenResult) {
                            mToken = getTokenResult.getToken(); //makeEndpointsRequest(getTokenResult.getToken());
                            Log.d(TAG, "onSuccess " + mToken);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(TAG, "onFailure Failed to get token from Firebase.");
                        }
                    });
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
            }
        };
    }

    public String getToken() {
        return mToken;
    }

    public String getUid() {
        return mAuth.getCurrentUser().getUid();
    }

    public void onStart() {
        // add listener
        if (mAuth != null && mAuthListener != null) {
            mAuth.addAuthStateListener(mAuthListener);
        }
    }

    public void onStop() {
        // remove listener
        if (mAuth != null && mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    public void signIn(String email, String password, final IAuthRequestActivity authRequestActivity) {
        Log.d(TAG, "signIn " + email);

        if (mAuth == null || mAuthListener == null) {
            return;
        }

        authRequestActivity.showProgress(true);

        // sign in withemail
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener((Activity) authRequestActivity, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithEmail:onComplete:" + task.isSuccessful());

                        authRequestActivity.showProgress(false);

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithEmail:failed", task.getException());
                            authRequestActivity.setError(R.string.error_incorrect_password);
                        }
                    }
                });
    }

    private void createAccount(String email, String password, final IAuthRequestActivity authRequestActivity) {
        Log.d(TAG, "createAccount " + email);

        authRequestActivity.showProgress(true);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener((Activity) authRequestActivity, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful());

                        authRequestActivity.showProgress(false);

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            authRequestActivity.setError(R.string.error_incorrect_password);
                        }
                    }
                });
    }
}

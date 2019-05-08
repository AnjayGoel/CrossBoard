package com.anjay.crossboard;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class back_service extends Service {
    public back_service() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}

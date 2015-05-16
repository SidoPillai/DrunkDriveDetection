package drunkdrive.siddesh.drunkdrive;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class DrunkDriveSettings extends PreferenceActivity {

    public void onCreate(Bundle b) {
        super.onCreate(b);
        addPreferencesFromResource(R.xml.settings);
    }

}

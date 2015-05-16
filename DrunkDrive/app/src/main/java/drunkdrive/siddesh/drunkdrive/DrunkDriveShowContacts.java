package drunkdrive.siddesh.drunkdrive;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Contacts;
import android.provider.Contacts.People;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class DrunkDriveShowContacts extends Activity implements OnItemClickListener {
    DrunkDriveDatabase database;
    ListView lview;
    ArrayAdapter<String> adaptor;

    public void onCreate(Bundle ic) {
        super.onCreate(ic);


    }

    public void onStart() {
        super.onStart();
        setContentView(R.layout.socialcontactlist);
        lview = (ListView) findViewById(R.id.lstview);
        adaptor = new ArrayAdapter<String>(this, R.layout.name);
        lview.setAdapter(adaptor);
        lview.setOnItemClickListener(this);
        fetchContacts();

    }

    public void fetchContacts() {
        int i = 0;
        String s;
        database = new DrunkDriveDatabase(getBaseContext());
        SQLiteDatabase db = database.getReadableDatabase();
        Cursor c = db.query(DrunkDriveDatabase.TABLE_NAME, new String[]{DrunkDriveDatabase.COLUMN_NAME, DrunkDriveDatabase.COLUMN_NUMBER}, null, null, null, null, null);
        while (c.moveToNext()) {
            i = 1;
            s = c.getString(0) + "\n\n" + c.getString(1);
            adaptor.add(s);

        }
        if (i == 0) {
            Toast.makeText(this, "Contacts List Empty", 5000).show();
        }
    }

    public boolean onCreateOptionsMenu(Menu m) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu1, m);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem itm) {
        if (itm.getItemId() == R.id.add) {
            Intent i = new Intent(this, DrunkDriveAddContact.class);
            startActivity(i);

        } else {
            Intent i1 = new Intent(this, DrunkDriveDeleteContacts.class);
            startActivity(i1);
        }

        return true;
    }

    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {


    }


}

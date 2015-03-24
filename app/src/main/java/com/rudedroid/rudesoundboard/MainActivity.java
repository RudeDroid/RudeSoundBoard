package com.rudedroid.rudesoundboard;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;

import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.rudedroid.rudesoundboard.data.CustomSound;
import com.rudedroid.rudesoundboard.data.SoundboardManager;

import roboguice.activity.RoboActivity;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;

@ContentView(R.layout.activity_main)
public class MainActivity extends RoboActivity {
    @InjectView(R.id.txtSoundTitle)
    private TextView txtSoundTitle;
    @InjectView(R.id.btnPlay)
    private ImageButton btnPlay;
    @InjectView(R.id.btnStop)
    private ImageButton btnStop;
    @InjectView(R.id.btnShuffle)
    private ImageButton btnShuffle;
    @InjectView(R.id.btnShare)
    private ImageButton btnShareSound;
    @InjectView(R.id.btnRingtone)
    private ImageButton btnRingtone;

    private SoundboardManager soundManager;
    private Context mContext;

    //Manejo de los sonidos
    private int numBoton = -1;
    private File path;
    private String filename;
    private MediaPlayer mediaPlayer = null;

    //Menu lateral
    private BarneyAdapter adapter;
    private LayoutInflater mInflater;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private LinearLayout mDrawerContainer;
    private ActionBarDrawerToggle mDrawerToggle;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = this;
        soundManager = SoundboardManager.getInstance(this);

        //Boton play
        btnPlay.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                playSound();
            }
        });

        //Boton stop
        btnStop.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                stopSound();
            }
        });

        //Boton shuffle
        btnShuffle.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                shuffleSound();
            }
        });

        //Boton compartir sonido
        btnShareSound.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                shareSound();
            }
        });

        //Boton establecer como tono del telefono
        btnRingtone.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (numBoton != -1) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle(soundManager.soundsList.get(numBoton).getTitle());
                    final String[] items = {getResources().getString(R.string.strRingTone), getResources().getString(R.string.strNotifTone)};

                    builder.setItems(items, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int item) {
                            setSoundAs(item);
                        }
                    });

                    Dialog dialogAux = builder.create();
                    dialogAux.show();
                }
            }
        });

        //Menu lateral
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerContainer = (LinearLayout) findViewById(R.id.left_drawer_cont);
        mDrawerList = (ListView) findViewById(R.id.menu_links);

        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);

        adapter = new BarneyAdapter(mContext);
        mDrawerList.setAdapter(adapter);

        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.drawable.ic_drawer,  /* nav drawer image to replace 'Up' caret */
                R.string.drawer_open,  /* "open drawer" description for accessibility */
                R.string.drawer_close  /* "close drawer" description for accessibility */
        ) {
            public void onDrawerClosed(View view) {
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View drawerView) {
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        mDrawerLayout.openDrawer(mDrawerContainer);
    }

    @Override
    public void onBackPressed() {
        stopSound();
        super.onBackPressed();
    }


    //Funciones de los botones del panel de control inferior\\

    //Funcion para reproducir un sonido
    public void playSound() {
        stopSound();
        if (numBoton != -1) {
            mediaPlayer = MediaPlayer.create(MainActivity.this, getResources().getIdentifier(soundManager.soundsList.get(numBoton).getSound(), "raw", getPackageName()));
            mediaPlayer.start();
            mediaPlayer.setOnCompletionListener(new OnCompletionListener() {

                public void onCompletion(MediaPlayer mp) {

                }
            });
        }
    }

    //Funcion para detener un sonido
    public void stopSound() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    //Funcion para reproducir un sonido aleatorio
    public void shuffleSound() {
        int numFrases = soundManager.soundsList.size();
        int aux = (int) (Math.random() * numFrases);

        while (numBoton == aux) {
            aux = (int) (Math.random() * numFrases);
        }

        txtSoundTitle.setText(soundManager.soundsList.get(aux).getTitle());
        mDrawerList.setItemChecked(aux, true);

        numBoton = aux;
        playSound();
    }

    //Funcion para compartir el sonido
    public void shareSound() {
        File aux = createFileInStorage();
        if (aux != null) {
            Intent share = new Intent(Intent.ACTION_SEND);
            share.setType("audio/mp3");
            share.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.strShareSoundTitle));
            share.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(aux));
            startActivity(Intent.createChooser(share, getResources().getString(R.string.strShareSound) + " - " + soundManager.soundsList.get(numBoton).getTitle()));
        }
    }

    //Funcion para asignar sonido como tono de llamada o notificaciones (type=0 para llamada y 1 para notificaciones)
    public void setSoundAs(int type) {
        File aux = createFileInStorage();
        if (aux != null) {
            ContentValues values = new ContentValues();
            values.put(MediaStore.MediaColumns.DATA, aux.getAbsolutePath());
            values.put(MediaStore.MediaColumns.TITLE, soundManager.soundsList.get(numBoton).getSound());
            values.put(MediaStore.MediaColumns.MIME_TYPE, "audio/mp3");
            values.put(MediaStore.Audio.Media.ARTIST, getResources().getString(R.string.title_activity_main));
            values.put(MediaStore.Audio.Media.IS_RINGTONE, true);
            values.put(MediaStore.Audio.Media.IS_NOTIFICATION, true);
            values.put(MediaStore.Audio.Media.IS_ALARM, false);
            values.put(MediaStore.Audio.Media.IS_MUSIC, false);

            Uri uri = MediaStore.Audio.Media.getContentUriForPath(aux.getAbsolutePath());
            getContentResolver().delete(uri, MediaStore.MediaColumns.DATA + "=\"" + aux.getAbsolutePath() + "\"", null);
            Uri uriMp3 = this.getContentResolver().insert(uri, values);

            switch (type) {
                case 0:
                    RingtoneManager.setActualDefaultRingtoneUri(this, RingtoneManager.TYPE_RINGTONE, uriMp3);
                    break;
                case 1:
                    RingtoneManager.setActualDefaultRingtoneUri(this, RingtoneManager.TYPE_NOTIFICATION, uriMp3);
                    break;
            }
            checkRingtoneAndNotificationTheSame(type, uriMp3);
        }
    }

    //Funcion para corregir un error que se da si al cambiar el tono de llamada el de notificaciones era el mismo sonido y viceversa
    //El error es que se quita el tono que habia del otro tipo y se pone al valor predeterminado del telefono
    public void checkRingtoneAndNotificationTheSame(int type, Uri uriMp3) {
        SharedPreferences pf = getSharedPreferences("misPreferencias", MODE_PRIVATE);
        Editor editor = getSharedPreferences("misPreferencias", MODE_PRIVATE).edit();
        int idNotification = pf.getInt("idLastNotification", -1);
        int idRingtone = pf.getInt("idLastRingtone", -1);

        switch (type) {
            case 0:
                if (idNotification == numBoton) {
                    RingtoneManager.setActualDefaultRingtoneUri(this, RingtoneManager.TYPE_NOTIFICATION, uriMp3);
                }
                editor.putInt("idLastRingtone", numBoton);
                break;
            case 1:
                if (idRingtone == numBoton) {
                    RingtoneManager.setActualDefaultRingtoneUri(this, RingtoneManager.TYPE_RINGTONE, uriMp3);
                }
                editor.putInt("idLastNotification", numBoton);
                break;
        }
        editor.commit();
    }

    //Funcion que comprueba si el archivo a enviar ya esta guardado en la memoria interna y si no es asi lo guarda
    public File createFileInStorage() {
        if (numBoton != -1) {
            path = setupExternalCacheDir();
            filename = soundManager.soundsList.get(numBoton).getSound() + ".mp3";
            File aux = new File(path, filename);
            if (!aux.exists()) {
                if (!savering(aux)) {
                    Toast.makeText(this, getResources().getString(R.string.strErrorSaving), Toast.LENGTH_SHORT).show();
                    return null;
                }
            }

            return aux;
        } else {
            return null;
        }
    }


    //Adapter de la lista de sonidos
    public class BarneyAdapter extends ArrayAdapter<CustomSound> {
        public BarneyAdapter(Context context) {
            super(context, R.layout.drawer_list_item, soundManager.soundsList);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final int pos = position;
            mInflater = LayoutInflater.from(getApplicationContext());
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.drawer_list_item, null);
            }

            ((TextView) convertView).setText(soundManager.soundsList.get(position).getTitle());
            convertView.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    txtSoundTitle.setText(soundManager.soundsList.get(pos).getTitle());
                    mDrawerList.setItemChecked(pos, true);
                    mDrawerLayout.closeDrawer(mDrawerContainer);

                    numBoton = pos;
                    playSound();
                }
            });

            return convertView;
        }
    }


    //Menu desplegable de la ActionBar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        switch (item.getItemId()) {
            case R.id.contact:
                contactUs();
                break;
            case R.id.shareApp:
                shareApp();
                break;
            case R.id.rate:
                rateApp();
                break;
            case R.id.info:
                showInformation();
                break;
            case R.id.moreCharacters:
                moreCharacters();
                break;
        }

        return true;
    }

    //Para que muestre los iconos del menú :S
    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        if (featureId == Window.FEATURE_ACTION_BAR && menu != null) {
            if (menu.getClass().getSimpleName().equals("MenuBuilder")) {
                try {
                    Method m = menu.getClass().getDeclaredMethod(
                            "setOptionalIconsVisible", Boolean.TYPE);
                    m.setAccessible(true);
                    m.invoke(menu, true);
                } catch (NoSuchMethodException e) {
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return super.onMenuOpened(featureId, menu);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }


    //Funciones llamadas desde las opciones del menu\\

    //Funcion para contactar con RudeDroid
    private void contactUs() {
        final Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.strRecomendation));
        emailIntent.setType("plain/text");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{getResources().getString(R.string.strEmailAccount)});
        startActivity(Intent.createChooser(emailIntent, getResources().getString(R.string.strContact)));
    }

    //Funcion para compartir la app con los contactos
    public void shareApp() {
        Intent shareApp = new Intent(Intent.ACTION_SEND);
        shareApp.setType("text/plain");
        shareApp.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.strShareAppTitle));
        shareApp.putExtra(Intent.EXTRA_TEXT, getResources().getString(R.string.strShareAppText));
        startActivity(Intent.createChooser(shareApp, getResources().getString(R.string.strShareApp)));
    }

    //Funcion para puntuar la app en Google Play
    private void rateApp() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(getResources().getString(R.string.strAppUrl)));
        startActivity(intent);
    }

    //Funcion para acceder a Google Play a la cuenta de RudeDroid
    public void moreCharacters() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(getResources().getString(R.string.strRudeDroidUrl)));
        startActivity(intent);
    }

    //Funcion para mostrar la informacion de la app
    private void showInformation() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
        alertDialog.setMessage(getResources().getString(R.string.strInfoText));
        alertDialog.setTitle(getResources().getString(R.string.strInfo));
        alertDialog.setIcon(android.R.drawable.ic_dialog_info);
        alertDialog.show();
    }


    //Funciones para acceder a la memoria del telefono\\

    //Funcion para guardar sonidos en la memoria interna o la tarjeta SD
    private boolean savering(File file) {
        if (numBoton == -1)
            return false;

        byte[] buffer = null;
        InputStream fIn = getBaseContext().getResources().openRawResource(getResources().getIdentifier(soundManager.soundsList.get(numBoton).getSound(), "raw", getPackageName()));
        int size = 0;
        try {
            size = fIn.available();
            buffer = new byte[size];
            fIn.read(buffer);
            fIn.close();
        } catch (IOException e) {
            return false;
        }

        boolean exists = path.exists();
        if (!exists) path.mkdirs();

        FileOutputStream save;
        try {
            save = new FileOutputStream(file);
            save.write(buffer);
            save.flush();
            save.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            return false;
        }

        return true;
    }

    //Funcion para acceder al directorio correcto a la hora de guardar los sonidos en la memoria interna o la tarjeta SD
    private File setupExternalCacheDir() {
        File extCacheDir;

        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
                extCacheDir = getExternalCacheDir();
            } else {
                extCacheDir = new File(Environment.getExternalStorageDirectory(), "/Android/data/" + getPackageName() + "/cache/");
                extCacheDir.mkdirs();
            }
        } else {
            extCacheDir = getFilesDir();
        }

        return extCacheDir;
    }
}

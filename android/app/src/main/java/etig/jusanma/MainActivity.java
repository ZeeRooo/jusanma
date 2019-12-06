package etig.jusanma;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.radiobutton.MaterialRadioButton;
import com.google.android.material.snackbar.Snackbar;
import com.triggertrap.seekarc.SeekArc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;


public class MainActivity extends AppCompatActivity {

    private short velocity;

    private BluetoothSocket bluetoothSocket;
    private View rootView;
    private String address;
    private BluetoothAdapter bluetoothAdapter;
    private MaterialButton appCompatImageButtonForward, appCompatImageButtonLeft, appCompatImageButtonRight, appCompatImageButtonBackward, fireAppCompatImageButton;
    private TextView distanceTextView;
    private short multiplier;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rootView = findViewById(R.id.rootView);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        appCompatImageButtonForward = findViewById(R.id.button_forward);

        appCompatImageButtonLeft = findViewById(R.id.button_left);

        appCompatImageButtonRight = findViewById(R.id.button_right);

        appCompatImageButtonBackward = findViewById(R.id.button_backward);

        TextView textViewCurrentVel = findViewById(R.id.textView_seekbar);

        View seekbarVelocityRootView = findViewById(R.id.velocity_seekbar_rootView);
        SeekBar velSeekbar = seekbarVelocityRootView.findViewById(R.id.seekbar_velocity);
        velSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                textViewCurrentVel.setText(String.valueOf(i));
                textViewCurrentVel.setX(seekbarVelocityRootView.getX() + ((i * (seekBar.getWidth() - 3 * seekBar.getThumbOffset())) / seekBar.getMax()));

                velocity = (byte) i;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                textViewCurrentVel.setY(seekbarVelocityRootView.getY() - 80);

                textViewCurrentVel.setVisibility(View.VISIBLE);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                textViewCurrentVel.setVisibility(View.GONE);
            }
        });

        View seekbarLightRootView = findViewById(R.id.seekbar_light_rootView);

        ImageView imageViewIcon = seekbarLightRootView.findViewById(R.id.imageView_seekbar_light);
        imageViewIcon.setImageDrawable(getDrawable(R.drawable.ic_light_off));

        VerticalSeekBar lightSeekbar = seekbarLightRootView.findViewById(R.id.seekbar_light);
        lightSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                textViewCurrentVel.setText(String.valueOf(i));
                textViewCurrentVel.setX(seekbarLightRootView.getX() + seekBar.getWidth());
                textViewCurrentVel.setY(seekBar.getHeight() - ((i * (seekBar.getHeight() - 3 * seekBar.getThumbOffset())) / seekBar.getMax()));

                writeStream("Q" + i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                textViewCurrentVel.setVisibility(View.VISIBLE);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                textViewCurrentVel.setVisibility(View.GONE);

                if (seekBar.getProgress() == 0)
                    imageViewIcon.setImageDrawable(getDrawable(R.drawable.ic_light_off));
                else imageViewIcon.setImageDrawable(getDrawable(R.drawable.ic_light_on));
            }
        });

        /*VerticalSeekBar verticalSeekBarTorrent = findViewById(R.id.seekbar_vertical_torrent);
        verticalSeekBarTorrent.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                textViewCurrentVel.setText(String.valueOf(i));
                textViewCurrentVel.setX(seekBar.getX() + seekBar.getWidth());
                textViewCurrentVel.setY(seekBar.getHeight() - ((i * (seekBar.getHeight() - 3 * seekBar.getThumbOffset())) / seekBar.getMax()));

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

                textViewCurrentVel.setVisibility(View.VISIBLE);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                writeStream("H" + seekBar.getProgress());
                textViewCurrentVel.setVisibility(View.GONE);
            }
        });*/


        View torrentSeekbarRootView = findViewById(R.id.torrent_seekbar_rootview);
        fireAppCompatImageButton = torrentSeekbarRootView.findViewById(R.id.button_fire);
        fireAppCompatImageButton.setOnClickListener((view -> writeStream("C")));

        MaterialRadioButton rightMaterialRadioButton = findViewById(R.id.right_radiobutton);

        SeekArc torrentSeekbar = torrentSeekbarRootView.findViewById(R.id.seekbar_torrent);
        torrentSeekbar.setOnSeekArcChangeListener(new SeekArc.OnSeekArcChangeListener() {
            @Override
            public void onProgressChanged(SeekArc seekArc, int progress, boolean fromUser) {
                textViewCurrentVel.setText(String.valueOf(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekArc seekArc) {
                fireAppCompatImageButton.setVisibility(View.GONE);
                textViewCurrentVel.setX(torrentSeekbarRootView.getX() + (float) (seekArc.getWidth() / 2.5));
                textViewCurrentVel.setY(torrentSeekbarRootView.getY() + (float) (seekArc.getHeight() / 2.5));
                textViewCurrentVel.setVisibility(View.VISIBLE);
            }

            @Override
            public void onStopTrackingTouch(SeekArc seekArc) {
                textViewCurrentVel.setVisibility(View.GONE);
                fireAppCompatImageButton.setVisibility(View.VISIBLE);

                if (rightMaterialRadioButton.isChecked())
                    multiplier = -1;
                else
                    multiplier = 1;
                writeStream("T" + seekArc.getProgress() * multiplier);

                seekArc.setProgress(0, true);
            }
        });

        distanceTextView = findViewById(R.id.distance_textView);

        getSupportActionBar().setSubtitle("Desconectado");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_activity_toolbar_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.item_connect) {
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (bluetoothAdapter.isDiscovering())
                bluetoothAdapter.cancelDiscovery();
            bluetoothAdapter.startDiscovery();

            ArrayList<BluetoothDevice> bluetoothDeviceArrayList = new ArrayList<>(bluetoothAdapter.getBondedDevices());

            for (byte a = 0; a < bluetoothDeviceArrayList.size(); a++) {
                if (bluetoothDeviceArrayList.get(a).getName().startsWith("HC"))
                    address = bluetoothDeviceArrayList.get(a).getAddress();
            }

            if (address != null)
                new ConnectBluetoothTask().execute();
            else
                Snackbar.make(rootView, "No se encontro el robot", Snackbar.LENGTH_LONG).show();
        }
        return super.onOptionsItemSelected(item);
    }

    private void writeStream(String action) {
        try {
            bluetoothSocket.getOutputStream().write(action.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception npe) {
            npe.printStackTrace();
            Snackbar.make(rootView, "No está conectado", Snackbar.LENGTH_LONG).show();
            getSupportActionBar().setSubtitle("Desconectado");
        }
    }

    private class ConnectBluetoothTask extends AsyncTask<Boolean, Boolean, Boolean> {

        @Override
        protected void onPreExecute() {
            Snackbar.make(rootView, "Conectando...", Snackbar.LENGTH_LONG).show();
        }

        @Override
        protected Boolean doInBackground(Boolean... bool) {
            try {
                BluetoothDevice bluetoothDevice = bluetoothAdapter.getRemoteDevice(address);
                bluetoothSocket = bluetoothDevice.createInsecureRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
                bluetoothAdapter.cancelDiscovery();
                bluetoothSocket.connect();
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (!result) {
                Snackbar.make(rootView, "Error de conexión", Snackbar.LENGTH_LONG).show();
            } else {
                Snackbar.make(rootView, "Conectado", Snackbar.LENGTH_LONG).show();
                getSupportActionBar().setSubtitle("Conectado");
                new ReadInputTask().execute();
            }
        }
    }

    private class ReadInputTask extends AsyncTask<String, String, String> {
        private int bytes;
        private byte[] buffer = new byte[256];
        private String readMessage;

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            if (values[0].equals("d"))
                getSupportActionBar().setSubtitle("Desconectado");
            else
                distanceTextView.setText("Distancia [cm]: " + values[0]);
        }

        @SuppressLint("WrongThread")
        @Override
        protected String doInBackground(String... voids) {
            while (bluetoothSocket.isConnected()) {
                try {
                    bytes = bluetoothSocket.getInputStream().read(buffer);
                    readMessage = new String(buffer, 0, bytes);
                    if (readMessage.contains("dist"))
                        publishProgress(readMessage.replaceAll("dist", "").replaceAll("-", ""));

                    if (appCompatImageButtonForward.isPressed()) {
                        writeStream("F" + velocity);
                    } else if (appCompatImageButtonBackward.isPressed()) {
                        writeStream("B" + velocity);
                    } else if (appCompatImageButtonRight.isPressed()) {
                        writeStream("R" + velocity);
                    } else if (appCompatImageButtonLeft.isPressed()) {
                        writeStream("L" + velocity);
                    } else
                        writeStream("S");

                } catch (Exception e) {
                    e.printStackTrace();
                    Snackbar.make(rootView, "Error de conexión", Snackbar.LENGTH_LONG).show();
                    try {
                        bluetoothSocket.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                    publishProgress("d");
                    break;
                }
            }
            return null;
        }
    }
}

package com.example.applicacioncamarero.activity;

import android.Manifest;
import android.animation.Animator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.provider.Settings.Secure;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.applicacioncamarero.BuildConfig;
import com.example.applicacioncamarero.R;
import com.example.applicacioncamarero.adapter.CamarerosAdapter;
import com.example.applicacioncamarero.adapter.MesasAdapter;
import com.example.applicacioncamarero.adapter.SalasAdapter;
import com.example.applicacioncamarero.connexion.DatabaseManipulation;
import com.example.applicacioncamarero.connexion.SQLServerManipulation;
import com.example.applicacioncamarero.connexion.SQLiteManipulation;
import com.example.applicacioncamarero.connexion.SQLiteTPVR;
import com.example.applicacioncamarero.dataClasses.Situacion;
import com.example.applicacioncamarero.dataClasses.Usuario;
import com.example.applicacioncamarero.model.Mesa;
import com.example.applicacioncamarero.tools.appActivation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    // Constantes
    private final int PERMISSIONS_REQUEST_ALL = 99; // On a demandé toutes les permissions d'un seul coup
    private final int PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 10;
    private final int PERMISSIONS_REQUEST_ACCESS_NETWORK_STATE = 11;
    private final int PERMISSIONS_REQUEST_INTERNET = 12;
    private final int PERMISSIONS_REQUEST_READ_PHONE_STATE = 13;


    public static int appActivated = 0;
    private static boolean dontReloadTables = false;


    /// Variables
    RecuperarSalas recuperarSalas;
    RecuperarMesas recuperarMesas;
    Context m_context = this;
    MainActivity m_mainActiviy = this;
    private CoordinatorLayout m_coordinatorLayout;
    private Snackbar msgSnackBar;
    public static String m_ANDROID_ID = "android_id";
    static private Situacion m_situacion = null;
    static private Situacion m_prev_situacion = null;

    //private static final String PREF_UNIQUE_ID = "PREF_UNIQUE_ID";
    //private static String cryptoPass = "Th3Quick8rownFoxJump50vertheLa2yDo9";
    //private static String iv = "0033647495580";

    //The main spinner

    Spinner m_spinnerSalas = null;


    //the recyclerview
    RecyclerView recyclerViewMesas;
    private String m_camarero = "";
    private BottomSheetDialog mBottomSheetDialog;
    //private CamarerosAdapter camarerososAdapter;
    private List<String> camareros = new ArrayList<>();
    RecyclerView camarerosRecyclerView;
    NavigationView navigationView;

    private String IP = "192.168.100.103";
    private String PORT = "1433";
    private String DB = "Di00001";
    private String DBTPVR = "TPVR";
    private String INSTANCE = "SQLEXPRESS";
    private String UN = "sa";
    private String PASSWORD = "11888";
    private String CAJA = "01";
    private String ALMACEN = "01";
    private String EMPRESA = "00001";
    private String CLIENTE = "00001";
    private String START_ROOM = "";
    private boolean OFFLINE = false;
    private SwipeRefreshLayout pullToRefresh;
    private Dialog m_progressDialog;


    private boolean parametersChanged = false;

    private static ArrayList<Mesa> mesasSeleccionadas = new ArrayList<Mesa>();


    FloatingActionButton fab, fab1, fab2, fab3;

    LinearLayout fabLayout1, fabLayout2, fabLayout3;

    boolean isFABOpen = false;


    // Activation de l'APP
    public class activateApp extends appActivation {

        public activateApp(Activity activity, String keyFile) {
            super(activity, keyFile);

        }

        public activateApp(Activity activity, String keyFile, String uniqueID) {
            super(activity, keyFile, uniqueID);
        }

        @Override
        public void onAppActivated() {
            appActivated = 1;
            // Cacher ACTIVAR si ce n'est la version DISENY
            if(navigationView != null) {
                Menu menuNav = navigationView.getMenu();
                if(menuNav != null) {
                    MenuItem nav_item = menuNav.findItem(R.id.nav_activate);
                    if(nav_item != null)
                          nav_item.setVisible(false);

                }
            }
        }

        @Override
        public void onDlgResultOK() {
            appActivated = m_result;
        }

        @Override
        public void onDlgResultCancel() {
            appActivated = 0;
        }

        public void checkResult() {
            // On vérifie le résultat et on prend une décision en conséquence
        }
    }

    activateApp activateapp;

    public boolean isEnviandoMesa() {
        return enviandoMesa;
    }

    public void setEnviandoMesa(boolean enviandoMesa) {
        this.enviandoMesa = enviandoMesa;
        if (!enviandoMesa) {
            mesasSeleccionadas.clear();
            msgSnackBar.dismiss();
            refrescaMesas();
        }
    }

    private boolean enviandoMesa;

    public void setSpinnerItemSelected(Spinner spinner, String item) {
        if (spinner != null && item != null) {
            Adapter adapter = spinner.getAdapter();

            for (int i = 0; i < adapter.getCount(); i++) {
                if (item.trim().equals(adapter.getItem(i).toString().trim())) {
                    spinner.setSelection(i);
                    break;
                }
            }
        }
    }


    protected void loadActivity() {


        // Chargement de la configuration du système
        SharedPreferences prefs = getApplicationContext().getSharedPreferences("SHARED_PREFERENCES", MODE_PRIVATE);
        CAJA = prefs.getString("CAJA", CAJA);//"No name defined" is the default value.
        ALMACEN = prefs.getString("ALMACEN", ALMACEN);//"No name defined" is the default value.
        EMPRESA = prefs.getString("EMPRESA", EMPRESA);//"No name defined" is the default value.
        CLIENTE = prefs.getString("CLIENTE", CLIENTE);//"No name defined" is the default value.
        OFFLINE = prefs.getBoolean("OFFLINE", OFFLINE);//"No name defined" is the default value.
        START_ROOM = prefs.getString("START_ROOM", START_ROOM);//"No name defined" is the default value.  Salle de depart

        // Attribution de la toolbar à la actionbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actBar = getSupportActionBar();
        if (actBar != null)
            actBar.setDisplayShowTitleEnabled(false);


        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        // Cacher ACTIVAR si ce n'est la version DISENY
        if (!BuildConfig.BUILD_VERSION.equals("DISENY")) {
            if (navigationView != null) {
                Menu menuNav = navigationView.getMenu();
                if (menuNav != null) {
                    MenuItem nav_item = menuNav.findItem(R.id.nav_activate);
                    if (nav_item != null)
                        nav_item.setVisible(false);
                }
            }
        }



                // Boutton rafraichir
        ImageButton imgBtn = findViewById(R.id.refreshButton);

        if(imgBtn != null)
            imgBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    DatabaseManipulation dbmap;

                    if (OFFLINE) {
                        dbmap = new SQLiteManipulation(m_mainActiviy, DBTPVR);
                    } else {
                        dbmap = new SQLServerManipulation(m_mainActiviy, DBTPVR);
                    }
                    dbmap.close();
                    mesasSeleccionadas.clear();
                    if (m_situacion == null || m_situacion.getDescription().equals("") || parametersChanged) {
                        recuperarSalas = new RecuperarSalas(m_mainActiviy, m_spinnerSalas, START_ROOM);
                        recuperarSalas.execute();
                    } else {
                        //getting the recyclerview from xml
                        recyclerViewMesas = findViewById(R.id.recyclerView);
                        recyclerViewMesas.setHasFixedSize(true);
                        recyclerViewMesas.setLayoutManager(new GridLayoutManager(m_context, 4));
                        /*recuperarMesas = new RecuperarMesas(m_mainActiviy, recyclerViewMesas);
                        recuperarMesas.execute();*/
                        recuperarSalas = new RecuperarSalas(m_mainActiviy, m_spinnerSalas, m_situacion.getDescription());
                        recuperarSalas.execute();
                    }
                }
            });

        //
        pullToRefresh = findViewById(R.id.pullToRefresh);
        pullToRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.d("Pull to refresh", "Refreshing");
                mesasSeleccionadas.clear();
                if (m_situacion == null || m_situacion.getDescription().equals("") || parametersChanged) {
                    recuperarSalas = new RecuperarSalas(m_mainActiviy, m_spinnerSalas, START_ROOM);
                    recuperarSalas.execute();
                } else {
                    //getting the recyclerview from xml
                    recyclerViewMesas = (RecyclerView) findViewById(R.id.recyclerView);
                    recyclerViewMesas.setHasFixedSize(true);
                    recyclerViewMesas.setLayoutManager(new GridLayoutManager(m_context, 4));
                    recuperarMesas = new RecuperarMesas(m_mainActiviy, recyclerViewMesas);
                    recuperarMesas.execute();
                }
                pullToRefresh.setRefreshing(false);
            }
        });

        // Creation du spinner salas et affectation du set on item selected
        m_spinnerSalas = findViewById(R.id.spinner);

        m_spinnerSalas.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @RequiresApi(api = Build.VERSION_CODES.CUPCAKE)
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                if (!dontReloadTables) {
                    // Se ha seleccionado una sala
                    m_prev_situacion = m_situacion;
                    m_situacion = (Situacion) parentView.getSelectedItem();
                    //m_sala = m_situacion.getDescription();
                    //TextView tv = (TextView) selectedItemView;
                    Log.d("Cod. sala seleccionada", m_situacion.getClave());
                    Log.d("Desc. sala seleccionada", m_situacion.getDescription());
                    //m_sala = tv.getText().toString();
                    recyclerViewMesas = (RecyclerView) findViewById(R.id.recyclerView);
                    recyclerViewMesas.setHasFixedSize(true);
                    recyclerViewMesas.setLayoutManager(new GridLayoutManager(m_context, 4));

                    showConnecting(R.string.connecting);
                    recuperarMesas = new RecuperarMesas(m_mainActiviy, recyclerViewMesas);
                    recuperarMesas.execute();

                    if (mesasSeleccionadas.size() > 0) {
                        if (!enviandoMesa) {
                            mesasSeleccionadas.clear();
                            if (isFABOpen) {
                                closeFABMenu(true);
                            }
                            Handler mHandler = new Handler();
                            mHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    fab.hide();
                                }
                            }, 400L);
                        }

                    }
                } else
                    dontReloadTables = false;


            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Rien n'a été sélectionné

            }

        });


        // Creation de la mesaList

        Log.d("ONCREATE", "Creacion de datos");
        recuperarSalas = new RecuperarSalas(this, m_spinnerSalas, START_ROOM);
        recuperarSalas.execute();

        View headerview = navigationView.getHeaderView(0);
        LinearLayout header = (LinearLayout) headerview.findViewById(R.id.header);
        header.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //showBottomSheetDialog();
                RecuperaCamareros recuperaCanareros = new RecuperaCamareros(true);
                recuperaCanareros.execute();
            }
        });

        fabLayout1 = (LinearLayout) findViewById(R.id.fabLayout1);

        fabLayout2 = (LinearLayout) findViewById(R.id.fabLayout2);

        fabLayout3 = (LinearLayout) findViewById(R.id.fabLayout3);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab1 = (FloatingActionButton) findViewById(R.id.fab1);
        fab2 = (FloatingActionButton) findViewById(R.id.fab2);
        fab3 = (FloatingActionButton) findViewById(R.id.fab3);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isFABOpen) {
                    showFABMenu();
                } else {
                    closeFABMenu(false);
                }
            }
        });


        fab1.setOnClickListener(new View.OnClickListener() { // Move table

            @Override
            public void onClick(View view) { // Move table
                Mesa sourceTable;
                String error = "";


                if (mesasSeleccionadas.size() > 1) {
                    error = getResources().getString(R.string.move_only_one_table);
                } else {
                    sourceTable = mesasSeleccionadas.get(0);

                    if ((!sourceTable.getState().equals("P") && !sourceTable.getState().equals("P")) || sourceTable.getLinkedTable() > 0) {
                        error = getResources().getString(R.string.table_not_moveable);

                    }
                }


                if (!error.isEmpty()) {
                    // On affiche le message d'erreur
                    AlertToast(error);

                } else  // On passe en mode selection de table
                {
                    enviandoMesa = true;
                    msgSnackBar = Snackbar.make(m_coordinatorLayout, "Moviendo mesa", Snackbar.LENGTH_INDEFINITE);
                    Snackbar.SnackbarLayout layout = (Snackbar.SnackbarLayout) msgSnackBar.getView();
                    // Get the toast view
                    TextView textView = (TextView) layout.findViewById(android.support.design.R.id.snackbar_text);
                    textView.setVisibility(View.INVISIBLE);

                    // Inflate our custom view from custom layout
                    View snackView = getLayoutInflater().inflate(R.layout.layout_snackbar, null);
                    // Configure the view

                    /*ImageView imageView = (ImageView) snackView.findViewById(R.id.image);
                        imageView.setImageBitmap(image);
                    TextView textViewTop = (TextView) snackView.findViewById(R.id.text);
                    textViewTop.setText(text);
                    textViewTop.setTextColor(Color.WHITE);*/

                    // Adda pading to fit the full text in the view
                    // seems it does not work
                    layout.setPadding(10, 0, 10, 0);

                    // Add the view to the Snackbar's layout
                    layout.addView(snackView, 0);
                    // Show the Snackbar

                    msgSnackBar.show();

                    //RecyclerView.ViewHolder viewHolder = recyclerViewMesas.findViewHolderForAdapterPosition(mesasSeleccionadas.get(0).getTitle());
                    closeFABMenu(true);
                }
                pullToRefresh.setRefreshing(false);
            }
        });

        fabLayout1.setOnClickListener(new View.OnClickListener() { // Move table

            @Override
            public void onClick(View view) { // Move table
                Mesa sourceTable;
                String error = "";


                if (mesasSeleccionadas.size() > 1) {
                    error = getResources().getString(R.string.move_only_one_table);
                } else {
                    sourceTable = mesasSeleccionadas.get(0);


                    if ((!sourceTable.getState().equals("P") && !sourceTable.getState().equals("P")) || sourceTable.getLinkedTable() > 0) {
                        error = getResources().getString(R.string.table_not_moveable);

                    }
                }


                if (!error.isEmpty()) {
                    // On affiche le message d'erreur
                    AlertToast(error);
                    ;

                } else  // On passe en mode selection de table
                {
                    enviandoMesa = true;
                    msgSnackBar = Snackbar.make(m_coordinatorLayout, "Moviendo mesa", Snackbar.LENGTH_INDEFINITE);
                    Snackbar.SnackbarLayout layout = (Snackbar.SnackbarLayout) msgSnackBar.getView();
                    // Get the toast view
                    TextView textView = (TextView) layout.findViewById(android.support.design.R.id.snackbar_text);
                    textView.setVisibility(View.INVISIBLE);

                    // Inflate our custom view from custom layout
                    View snackView = getLayoutInflater().inflate(R.layout.layout_snackbar, null);
                    // Configure the view

                    /*ImageView imageView = (ImageView) snackView.findViewById(R.id.image);
                        imageView.setImageBitmap(image);
                    TextView textViewTop = (TextView) snackView.findViewById(R.id.text);
                    textViewTop.setText(text);
                    textViewTop.setTextColor(Color.WHITE);*/

                    // Adda pading to fit the full text in the view
                    // seems it does not work
                    layout.setPadding(10, 0, 10, 0);

                    // Add the view to the Snackbar's layout
                    layout.addView(snackView, 0);
                    // Show the Snackbar

                    msgSnackBar.show();

                    //RecyclerView.ViewHolder viewHolder = recyclerViewMesas.findViewHolderForAdapterPosition(mesasSeleccionadas.get(0).getTitle());
                    closeFABMenu(true);
                }
                pullToRefresh.setRefreshing(false);
            }
        });


        fab3.setOnClickListener(new View.OnClickListener() { // Unir mesas
            @Override
            public void onClick(View view) { // Join tables
                if (isFABOpen) {
                    joinTables();
                    closeFABMenu(true);
                    pullToRefresh.setRefreshing(false);
                }
            }
        });


        fab2.setOnClickListener(new View.OnClickListener() { // Separar
            @Override
            public void onClick(View view) { // Disjoin tables
                if (isFABOpen) {
                    disjoinTables();
                    closeFABMenu(true);
                    pullToRefresh.setRefreshing(false);
                }
            }
        });


        askForWaiter(false);

        enviandoMesa = false;
        if (msgSnackBar != null)
            msgSnackBar.dismiss();
        pullToRefresh.setRefreshing(false);

    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        activateApp activateapp = new activateApp(this, "config.txt");

        switch (requestCode) {

            case PERMISSIONS_REQUEST_ALL: {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.length > 0)
                        && (grantResults[0] + grantResults[1] + grantResults[2] + grantResults[3] == PackageManager.PERMISSION_GRANTED)) {
                    // permission was granted, yay! Do the
                    // task you need to do.
                    Toast.makeText(getApplicationContext(), R.string.permision_granted, Toast.LENGTH_SHORT).show();
                    // Vérifier si le logiel a été activé pour la version diseny
                    // Ne pas tenir compte du warning
                    if (BuildConfig.BUILD_VERSION.equals("DISENY")) {
                        //getUniqueId(this);
                        appActivated = activateapp.checkActivation(this);
                    } else {
                        appActivated = 1;
                        // Cacher ACTIVAR si ce n'est la version DISENY
                        Menu menuNav = navigationView.getMenu();
                        MenuItem nav_item = menuNav.findItem(R.id.nav_activate);
                        nav_item.setVisible(false);
                        }

                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(getApplicationContext(), R.string.permision_denied, Toast.LENGTH_SHORT).show();
                }

            }

            break;
            case PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE:
                break;
            case PERMISSIONS_REQUEST_ACCESS_NETWORK_STATE:
                break;
            case PERMISSIONS_REQUEST_INTERNET:
                break;

            case PERMISSIONS_REQUEST_READ_PHONE_STATE:
                break;


        }
    }


    public void askForPermissions() {
        /* Demande d'autorisation pour écrire dans la mémoire externe */

        // On regarde d'abord si on n'a pas encore l'autorisation
        // Si c'est le premier lancement ou si l'utilisateur a refuser une première fois
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) + // Lecture écriture de fichiers
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE) + // Lecture écriture de fichiers
                ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) +
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
                // Lecture écriture de fichiers
                != PackageManager.PERMISSION_GRANTED) {

            // Nous n'avons pas l'autorisation


            // On regarde si l'utilisateur a déja refusé une fois
            // shouldShowRequestPermissionRationale retourne true si l'utilisateur a déja
            // dit non ou false si l'utilisateur a refusé et qu' il
            // a cliqué sur don't ask again
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_NETWORK_STATE)
                    || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.INTERNET)
                    || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_PHONE_STATE)) {
                // L'utilisateur a déja refusé une fois , on lui donne une explication de
                // pouquoi il doit accepter. L'explication doit être concise sous
                // forme de dialogue qui ne bloque pas le reste du processus.
                // Une fois que l'utilisateur a validé le dialogue on appelle ActivityCompat.requestPermissions

                // On affiche le message à l'utilisateur
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                alertDialogBuilder.setMessage(R.string.must_validate_permissions);
                alertDialogBuilder.setTitle(R.string.validate_permissions);
                alertDialogBuilder.setIcon(R.drawable.alert_512);

                alertDialogBuilder.setCancelable(false); // Not to be removed by the back button

                // Buttun Accept
                alertDialogBuilder.setPositiveButton(R.string.aceptar,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                ActivityCompat.requestPermissions(MainActivity.this,
                                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                                Manifest.permission.INTERNET,
                                                Manifest.permission.ACCESS_NETWORK_STATE,
                                                Manifest.permission.READ_PHONE_STATE},
                                        PERMISSIONS_REQUEST_ALL);   // PERMISSIONS_REQUEST_ALL est une constante que j'ai déficni au dénut du code
                                // et qui me sera renvoyée dans la callback "onRequestPermissionsResult" pour que je sache
                                // d'ou vient la demande. Si je faisait une demande ailleur dans le code je pourrais utiliser une autre constante...
                            }
                        });
                // Only one button ... Accept
                alertDialogBuilder.setNegativeButton(R.string.cancelar,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                finish();
                                moveTaskToBack(true);
                                System.exit(0);
                            }
                        });

                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();


            } else {
                // Si l'utilisateur n'a pas encore validé les autorisations, elles eront présentés
                // Si il avait répondu "Ne plus demander" les autorisations concernées seront refusés d'office
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.INTERNET,
                                Manifest.permission.ACCESS_NETWORK_STATE,
                                Manifest.permission.READ_PHONE_STATE},
                        PERMISSIONS_REQUEST_ALL);   // PERMISSIONS_REQUEST_ALL est une constante que j'ai déficni au dénut du code
                // et qui me sera renvoyée dans la callback "onRequestPermissionsResult" pour que je sache
                // d'ou vient la demande. Si je faisait une demande ailleur dans le code je pourrais utiliser une autre constante...


            }
        } else {
            // L'auttorisation a déjà été accordé précedement
            // on ne fait rien sauf vérifier que l'app est activée
            Toast.makeText(getApplicationContext(), "Permission granted", Toast.LENGTH_SHORT).show();
            if (BuildConfig.BUILD_VERSION.equals("DISENY")) {
                //getUniqueId(this);
                activateapp.checkActivation(this);
            } else {
                appActivated = 1;
            }
        }
    }


    public void showConnecting(int resourceString) {
        // On affiche un message pour faire patienter l'utilisateur


        if (m_progressDialog != null) {
            m_progressDialog.dismiss();
        }

        LayoutInflater inflater = this.getLayoutInflater();

        View dialogView = inflater.inflate(R.layout.progress_dialog, null);
        TextView tv = dialogView.findViewById(R.id.loading_msg);
        if (tv != null)
            tv.setText(resourceString);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        m_progressDialog = builder.create();
        m_progressDialog.setCancelable(false);

        m_progressDialog.show();
    }

    public void hideConnecting() {
        if (m_progressDialog != null && m_progressDialog.isShowing())
            m_progressDialog.hide();
    }


    @SuppressLint("HardwareIds")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        String id;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        activateapp = new activateApp(this, "config.txt");

        // Demande des autorisations
        //askForPermissions();





        /*  Envoyer les infos du unique ID */
        /*
        Intent sendMsgIntent = new Intent();
        sendMsgIntent.setPackage("com.whatsapp");
        sendMsgIntent.setAction(Intent.ACTION_SEND);
        sendMsgIntent.putExtra(Intent.EXTRA_TEXT, "Le message que tu veux envoyer");
        sendMsgIntent.setType("text/plain");
        this.startActivity(sendMsgIntent);
        */


        m_coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayoutMainActivity);

        mBottomSheetDialog = new BottomSheetDialog(this);

        m_ANDROID_ID = Settings.Secure.getString(this.getContentResolver(), Secure.ANDROID_ID);

        Log.d("ANDROID ID", m_ANDROID_ID);
        loadActivity();
        // Demande des autorisations
        askForPermissions();

    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {

        } else {

        }
        pullToRefresh.setRefreshing(false);

    }


    //Prevents a bug where tables would not be shown as selected after turning down the phone and and turning it on again,
    //but where the array of selected tables would not be cleared either.
    @Override
    protected void onPause() {
        //mesasSeleccionadas.clear();
        closeFABMenu(true);
        super.onPause();
        pullToRefresh.setRefreshing(false);
    }


    //This will be used later to display again the table as selected after turning the phone back on, but it is not a priority right now
    @Override
    protected void onResume() {
        super.onResume();
        refrescaMesas();
        pullToRefresh.setRefreshing(false);
    }

    public CoordinatorLayout getCoordinatoLayout() {
        return m_coordinatorLayout;
    }

    public void AlertToast(String error) {


        Toast toast = Toast.makeText(getApplication(), error, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 0);
        View view = toast.getView();
        //view.setBackgroundColor(getResources().getColor(android.R.color.holo_red_dark));
        view.setBackgroundResource(R.drawable.bg_custom_toast);
        //view.setBackgroundResource(R.drawable.bg_negative_corners);
        TextView text = view.findViewById(android.R.id.message);
        view.setPadding(10, 10, 10, 10);
        text.setTextColor(Color.rgb(255, 255, 255));
        text.setShadowLayer(20, 0, 0, Color.BLUE);
        text.setPadding(20, 5, 5, 5);

        LinearLayout.LayoutParams layoutparams = (LinearLayout.LayoutParams) view.getLayoutParams();
        if (layoutparams != null)
            layoutparams.setMargins(10, 2, 10, 2);

        toast.show();
    }

    public class AsyncJoinTable extends AsyncTask<String, String, String>
    {
        String result = "";
        DatabaseManipulation dbmap = null;

        public AsyncJoinTable() {
            super();
        }

        @Override
        protected String doInBackground(String... strings) {
            if (OFFLINE)
                dbmap = new SQLiteManipulation(m_mainActiviy, DBTPVR);
            else
                dbmap = new SQLServerManipulation(m_mainActiviy, DBTPVR);

            Log.d("Juntar", OFFLINE ? "Mode offline": "Mode online");
            String stDate;
            String query = "INSERT INTO TicketsHist ( \n" +
                    "       NTicket\n" +
                    "      ,Cliente\n" +
                    "      ,NCliente\n" +
                    "      ,Fecha\n" +
                    "      ,Caja\n" +
                    "      ,Vend\n" +
                    "      ,Estado\n" +
                    "      ,Situ\n" +
                    "      ,Mesa\n" +
                    "      ,Almacen\n" +
                    "      ,Empresa\n" +
                    "      )\n" +
                    "\t  VALUES\n" +
                    "\t  (?\n" + // Table comun
                    "\t  ,'00001'\n" + // Client
                    "\t  ,'CLIENTES VARIOS'\n" + // Nom client
                    "\t  ,CURRENT_TIMESTAMP\n" + // date
                    "\t  ,?\n" + // Caisse
                    "\t  ,?\n" + // Serveur
                    "\t  ,'J'\n" + // Etat = j
                    "\t  ,?\n" + // Ubi
                    "\t  ,?\n" + // La table qui se joint
                    "\t  ,?\n" + // Le dépot
                    "\t  ,?)"; // L'entreprise
            Log.d("Query", query);
            // Récupérer la date

            try {

                if (dbmap != null && mesasSeleccionadas.size()>0) {
                    String tableComune = mesasSeleccionadas.get(0).getTitle();
                    dbmap.beginTransaction();
                    dbmap.compileStatement(query);
                    try {
                        Mesa mesa;
                        for (int i = 0; i < mesasSeleccionadas.size(); ++i) {
                            mesa = mesasSeleccionadas.get(i);
                            Log.d("Joining table ", mesa.getTitle());

                            dbmap.setPrepStatString(1, tableComune);
                            dbmap.setPrepStatString(2, CAJA);
                            dbmap.setPrepStatString(3, m_camarero);
                            dbmap.setPrepStatString(4, mesa.getCodUbicacion());
                            dbmap.setPrepStatInt(5, Integer.parseInt(mesa.getTitle()));
                            dbmap.setPrepStatString(6, ALMACEN);
                            dbmap.setPrepStatString(7, EMPRESA);

                            dbmap.executeStmtInsert();
                        }
                        dbmap.setTransactionSuccessful();

                    } catch (Exception ex) {
                        result = getResources().getString(R.string.error_guardar_datos) + " - " + ex.getMessage();
                        Log.d("Joining 1",result);
                    }
                    dbmap.endTransaction();

                }
            } catch (Exception ex) {
                result = getResources().getString(R.string.error_data_base_connection) + " - " + ex.getMessage();
                Log.d("Joining 2",result);
            } finally {
                if (dbmap != null)
                    dbmap.close();

            }

            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String s) {
            //super.onPostExecute(s);
            // On cache le message au lieu de le détruire pour le remontrer quand necessaire
            hideConnecting();
            mesasSeleccionadas.clear();
            refrescaMesas();
            // Si pas d'erreur on raffraichi les tables
            if (result.isEmpty()) {
                refrescaMesas();
            } else {
                // On affiche le message d'erreur
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getBaseContext());
                alertDialogBuilder.setMessage(result);
                alertDialogBuilder.setTitle(R.string.joins_tables);
                alertDialogBuilder.setIcon(R.drawable.alert_512);

                alertDialogBuilder.setCancelable(false); // Not to be removed by the back button

                // Only one button ... Accept
                alertDialogBuilder.setPositiveButton(R.string.aceptar,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {

                            }
                        });

                AlertDialog alertDialog = alertDialogBuilder.create();
                if(alertDialog != null)
                     alertDialog.show();
            }

        }
    }

    public String joinTables() {
        String result = "";


        /*if (!dbmap.isConnected()) {

            result = getResources().getString(R.string.error_data_base_connection);
        } else {*/
            // érifier si on a selectionné plus d'une table
            if (mesasSeleccionadas.size() > 1) {
                if (m_camarero == null || m_camarero.isEmpty()) {
                    result = getResources().getString(R.string.choose_waiter);
                } else {
                    //Vérifier qu'aucune des tables n'est occupé ni jointe
                    for (int i = 0; i < mesasSeleccionadas.size() && result.isEmpty(); ++i) {
                        String state = mesasSeleccionadas.get(i).getState();
                        if (state.equals("P") || state.equals("C") || state.equalsIgnoreCase("E")) {
                            result = getResources().getString(R.string.select_busy_table_forbidden);
                        } else if (mesasSeleccionadas.get(i).getLinkedTable() > 0) {
                            result = getResources().getString(R.string.table_linked_yet);
                        }
                    }
                }
                // Si i n'y a pas d'erreur on enregistre les tables liées dans la base de donnée
                if (result.isEmpty()) {
                    showConnecting(R.string.connecting);
                    new AsyncJoinTable().execute();
                }
            } else
                result = getResources().getString(R.string.must_select_tow_or_more_tables);
       // }

        if (result.isEmpty()) {
            refrescaMesas();
        } else {
            // On affiche le message d'erreur
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setMessage(result);
            alertDialogBuilder.setTitle(R.string.joins_tables);
            alertDialogBuilder.setIcon(R.drawable.alert_512);

            alertDialogBuilder.setCancelable(false); // Not to be removed by the back button

            // Only one button ... Accept
            alertDialogBuilder.setPositiveButton(R.string.aceptar,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {


                        }
                    });

            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        }


        return result;
    }

    public class AsyncDisjoinTable extends AsyncTask<String, String, String>
    {
        String result = "";
        DatabaseManipulation dbmap = null;
        Mesa sourceTable;

        public AsyncDisjoinTable() {
            super();
        }

        @Override
        protected String doInBackground(String... strings) {
            sourceTable = mesasSeleccionadas.get(0);
            String query = String.format(Locale.FRANCE, "DELETE    FROM TicketsHist\n" +
                    "WHERE NTicket = '%d' \n" +
                    "AND Situ = '%s'", sourceTable.getLinkedTable(), sourceTable.getCodUbicacion());

            if (OFFLINE)
                dbmap = new SQLiteManipulation(m_mainActiviy, DBTPVR);
            else
                dbmap = new SQLServerManipulation(m_mainActiviy, DBTPVR);

            if (dbmap == null) {
                result = "Error in connection with SQL server";
            } else {

                try {
                    dbmap.rawWriteQuery(query);
                } catch (Exception ex) {
                    result = getResources().getString(R.string.error_conectar_datos) + ex.getLocalizedMessage();
                } finally {
                    if (dbmap != null)
                        dbmap.close();
                }

            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String s) {
            //super.onPostExecute(s);
            // On cache le message au lieu de le détruire pour le remontrer quand necessaire
            hideConnecting();
            mesasSeleccionadas.clear();
            refrescaMesas();
            // Si pas d'erreur on raffraichi les tables
            if (result.isEmpty()) {
                refrescaMesas();
            } else {
                // On affiche le message d'erreur
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getBaseContext());
                alertDialogBuilder.setMessage(result);
                alertDialogBuilder.setTitle(R.string.fab_disjoin_table);
                alertDialogBuilder.setIcon(R.drawable.alert_512);

                alertDialogBuilder.setCancelable(false); // Not to be removed by the back button

                // Only one button ... Accept
                alertDialogBuilder.setPositiveButton(R.string.aceptar,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {

                            }
                        });

                AlertDialog alertDialog = alertDialogBuilder.create();
                if(alertDialog != null)
                    alertDialog.show();
            }
        }
    }

    public void disjoinTables() {
        String error = "";
        Mesa sourceTable;
        DatabaseManipulation dbmap;

        if (mesasSeleccionadas.size() > 1) {
            error = getResources().getString(R.string.select_only_one);
        } else {
            sourceTable = mesasSeleccionadas.get(0);

            if (!sourceTable.getState().isEmpty()) {
                error = getResources().getString(R.string.table_not_empty);
            } else if (sourceTable.getLinkedTable() == 0) {
                error = "Esta mesa no esta unida.";
            }
        }

        if (!error.isEmpty()) {
            AlertToast(error);
        } else {
            showConnecting(R.string.connecting);
            new AsyncDisjoinTable().execute();
        }


    }

    public class AsyncMoveTable extends AsyncTask<String, String, String> {
        private Mesa m_sourceTable, m_dstTable;
        String result = "";
        String tableState;
        String nTicket = "";
        DatabaseManipulation dbmap;

        public AsyncMoveTable(Mesa sourceTable, Mesa dstTable) {
            m_sourceTable = sourceTable;
            m_dstTable = dstTable;
        }

        @Override
        protected void onPreExecute() {

        }


        @Override
        protected void onPostExecute(String r) {

            // On cache le message au lieu de le détruire pour le remontrer quand necessaire
            hideConnecting();
            // Si pas d'erreur on raffraichi les tables
            if (result.isEmpty()) {
                refrescaMesas();
            } else {
                // On affiche le message d'erreur
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getBaseContext());
                alertDialogBuilder.setMessage(result);
                alertDialogBuilder.setTitle(R.string.transfer_table);
                alertDialogBuilder.setIcon(R.drawable.alert_512);

                alertDialogBuilder.setCancelable(false); // Not to be removed by the back button

                // Only one button ... Accept
                alertDialogBuilder.setPositiveButton(R.string.aceptar,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {

                            }
                        });

                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
            }

        }


        @Override
        protected String doInBackground(String... strings) {
            // Get the ticketno from the table object
            if (OFFLINE)
                dbmap = new SQLiteManipulation(m_mainActiviy, DBTPVR);
            else
                dbmap = new SQLServerManipulation(m_mainActiviy, DBTPVR);

            if (dbmap != null) {
                try {
                    String query = String.format("UPDATE TicketsHist\n" +
                            "SET Mesa = %s, Situ = '%s' \n" +
                            "WHERE NTicket = '%s'", m_dstTable.getTitle(), m_dstTable.getCodUbicacion(), m_sourceTable.getTicket());
                    Log.d(" Mover mesa", query);
                    try {
                        dbmap.rawWriteQuery(query);
                        dbmap.close();
                    } catch (Exception ex) {
                        result = getResources().getString(R.string.error_conectar_datos) + ex.getLocalizedMessage();
                    }
                    //}
                } catch (Exception ex) {
                    result = getResources().getString(R.string.error_conectar_datos) + ex.getLocalizedMessage();
                } finally {
                    if (dbmap != null)
                        dbmap.close();
                }
            }

            return result;
        }
    }


    public String moveTable(Mesa sourceTable, Mesa dstTable) {
        String result = "";
        String tableState;
        String nTicket = "";
        Mesa destTable;
        DatabaseManipulation dbmap;
        AsyncMoveTable asyncmovetable;


        // On crée un copie de la table pour ne pas perturber la table dest
        // pour si ça ne se passe pas bien
        if (dstTable != null) {
            destTable = new Mesa(dstTable.getId(), dstTable.getTitle(),
                    dstTable.getShortdesc(), dstTable.getCodUbicacion(),
                    dstTable.getUbicacion(), dstTable.getTarifa(),
                    dstTable.estaOcupada(), dstTable.getTicket(),
                    dstTable.getImage(), dstTable.getLinkedTable(), dstTable.isBar());
        } else destTable = null;

        // On vérifie si la table est vide
        if (sourceTable != null) {
            tableState = sourceTable.getState();
            nTicket = sourceTable.getTicket();
        } else tableState = "";
        // Si la table est vide
        if (sourceTable == null || destTable == null || tableState.isEmpty() || tableState.equals("A") || nTicket.isEmpty()) {
            result = getResources().getString(R.string.table_empty);
        } else {
            // Vérifier si la table d edestination est le bar
            if (destTable.isBar()) {
                // Dans ce cas le numéro de la "Nouvelle table" prendra les trois derniers chiffre du ticket
                String destination = String.format(Locale.FRANCE, "%d", Integer.parseInt(sourceTable.getTicket().substring(sourceTable.getTicket().length() - 3)));
                destTable.setShortdesc(destination);
                destTable.setTitle(destTable.getShortdesc());
            }

            //Vérifier si la table de destination n'est pas occupée
            tableState = destTable.getState();
            // Si la table de estination est libre on continue
            if (tableState.isEmpty() || tableState.equals("A")) {
                if (sourceTable.getLinkedTable() > 0) {
                    result = getResources().getString(R.string.cant_move_linked_table);
                } else {

                    showConnecting(R.string.connecting);
                    asyncmovetable = new AsyncMoveTable(sourceTable, destTable);
                    asyncmovetable.execute();
                }

            } else // Sinon on affiche le message table dest non vide
            {
                result = getResources().getString(R.string.dest_table_not_empty);
            }
        }

        // Si pas d'erreur on raffraichi les tables
        if (result.isEmpty()) {
            refrescaMesas();
        } else {
            // On affiche le message d'erreur
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setMessage(result);
            alertDialogBuilder.setTitle(R.string.transfer_table);
            alertDialogBuilder.setIcon(R.drawable.alert_512);

            alertDialogBuilder.setCancelable(false); // Not to be removed by the back button

            // Only one button ... Accept
            alertDialogBuilder.setPositiveButton(R.string.aceptar,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {


                        }
                    });

            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        }

        if (result.equals(getResources().getString(R.string.cant_move_linked_table)) || result.equals(getResources().getString(R.string.table_empty)))
            result = "";
        return (result);
    }


    private void showFABMenu() {

        final int coef = 1;
        int height;
        isFABOpen = true;
        TextView tv = fabLayout1.findViewById(R.id.fabText1);


        // Affichier joindre des table selement si c'est  une salle ( on ne peut pas joindre des personnes ou alors c'est une partouse )
        if (m_situacion.getMesas() > 0) {
            fabLayout2.setVisibility(View.VISIBLE);
            fabLayout3.setVisibility(View.VISIBLE);
            if (tv != null)
                tv.setText(R.string.transfer_table);
        } else {
            if (tv != null)
                tv.setText(R.string.transfer_to_table);
        }

        fabLayout1.setVisibility(View.VISIBLE);


        fab.animate().rotationBy(180);

        /*fabLayout1.animate().translationY(-getResources().getDimension(R.dimen .standard_55));

        fabLayout2.animate().translationY(-getResources().getDimension(R.dimen.standard_100));*/

        // On doit rétablir les données de chaque fab
        // Bloc Pour obtenir sa vrai hauteur
        int wrapSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        fab.measure(wrapSpec, wrapSpec);
        // Fin Bloc qui permet d'obtenir les vrais dimentions de fab

        height = (fab.getMeasuredHeight() * 5) / 6; // On ne monte pas de toute la hauteur pour avoir un effet d'hombre. Seulement 98% de la hauteur du précedent

        fabLayout1.animate().translationY(-height);
        wrapSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        fab1.measure(wrapSpec, wrapSpec);
        height += (fab1.getMeasuredHeight() * 98) / 100; // On ne monte pas de toute la hauteur pour avoir un effet d'hombre
        fabLayout2.animate().translationY(-height * coef);
        wrapSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        fab2.measure(wrapSpec, wrapSpec);
        height += (fab2.getMeasuredHeight() * 98) / 100; // On ne monte pas de toute la hauteur pour avoir un effet d'hombre
        fabLayout3.animate().translationY(-height * coef).setListener(new Animator.AnimatorListener() {

            @Override

            public void onAnimationStart(Animator animator) {

            }


            @Override

            public void onAnimationEnd(Animator animator) {

                if (isFABOpen) {

                    fab.setImageDrawable(ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_minus));
                    fab.hide();
                    fab.show();

                }

/*                if (fab.getRotation() != -180) {

                    fab.setRotation(-180);

                }*/

            }

            @Override

            public void onAnimationCancel(Animator animator) {

            }


            @Override

            public void onAnimationRepeat(Animator animator) {

            }

        });


    }


    public void closeFABMenu(final boolean hide) {
        isFABOpen = false;
        fab.animate().rotation(0);
        fabLayout1.animate().translationY(0);
        fabLayout2.animate().translationY(0);
        fabLayout3.animate().translationY(0);
        fabLayout3.animate().translationY(0).setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                if (!isFABOpen) {
                    fabLayout1.setVisibility(View.GONE);
                    fabLayout2.setVisibility(View.GONE);
                    fabLayout3.setVisibility(View.GONE);
                    fab.setImageDrawable(ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_plus));

                    //It is necessary to hide the button anyway, due to a bug causing the icon of the FAB button to disappear at the end of the animation.
                    //The goal of the boolean here is to know whether or not the FAB needs to reappear again.
                    fab.hide();
                    if (!hide) {
                        fab.show();
                    }
                }
            }

            @Override
            public void onAnimationCancel(Animator animator) {
                closeFABMenu(hide);
            }

            @Override
            public void onAnimationRepeat(Animator animator) {
            }
        });
    }


    public ArrayList<Mesa> getMesasSeleccionadas() {
        return mesasSeleccionadas;
    }

    public FloatingActionButton getFab() {
        return fab;
    }


    public void onRestart() {
        super.onRestart();

        refrescaMesas();

        pullToRefresh.setRefreshing(false);
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }
        if (isFABOpen) {
            closeFABMenu(false);
        } else {
            super.onBackPressed();
        }
    }

    public void refrescaMesas() {

        if (m_situacion == null) {

            if (m_spinnerSalas != null && m_spinnerSalas.getCount() > 0) {
                m_situacion = (Situacion) m_spinnerSalas.getSelectedItem();
                if (m_situacion == null) {
                    m_spinnerSalas.setSelection(0);
                    m_situacion = (Situacion) m_spinnerSalas.getSelectedItem();
                }

            }
        }
        recyclerViewMesas = (RecyclerView) findViewById(R.id.recyclerView);

        recyclerViewMesas.setHasFixedSize(true);
        recyclerViewMesas.setLayoutManager(new GridLayoutManager(m_context, 4));
        showConnecting(R.string.connecting);
        recuperarMesas = new RecuperarMesas(m_mainActiviy, recyclerViewMesas);
        recuperarMesas.execute();
    }

    public void askForWaiter(boolean forceChange) {
        if (forceChange || m_camarero.isEmpty()) {
            RecuperaCamareros recuperaCanareros = new RecuperaCamareros(forceChange);
            recuperaCanareros.execute();
            /*final ActionBar actionBar = getActionBar();

            // actionBar
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);

            // titleTextView
            TextView titleTextView = new TextView(actionBar.getThemedContext());

            titleTextView.setText(camarero);
            //titleTextView.setTypeface( your_typeface);

            //titleTextView.setOtherProperties();

            // Add titleTextView into ActionBar
            actionBar.setCustomView(titleTextView);
            //setTitle(m_camarero);*/
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        final DrawerLayout drawer = findViewById(R.id.drawer_layout);
        Handler mHandler = new Handler();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                drawer.closeDrawer(GravityCompat.START);
            }
        }, 500L);


        if (id == R.id.nav_connexion) {

            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            CoordinatorLayout linearLayout = (CoordinatorLayout) inflater.inflate(R.layout.popup_conexion, null, false);

            CoordinatorLayout coordinatorLayout = findViewById(R.id.coordinatorLayoutMain);
            int widthConstraintLayout = coordinatorLayout.getWidth();
            int heightConstraintLayout = coordinatorLayout.getHeight();

            final PopupWindow pw = new PopupWindow(linearLayout, (int) (widthConstraintLayout * 0.70), (int) (heightConstraintLayout * 0.70), true);

            pw.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                pw.setElevation(10);
            }

            pw.setAnimationStyle(R.style.Animation);

            final EditText editTextIP = (EditText) linearLayout.findViewById(R.id.editTextIP);
            final EditText editTextPORT = (EditText) linearLayout.findViewById(R.id.editTextPORT);
            final EditText editTextDB = (EditText) linearLayout.findViewById(R.id.editTextDB);
            final EditText editTextDBRest = (EditText) linearLayout.findViewById(R.id.editTextDBRestaurante);
            final EditText editTextINSTANCE = (EditText) linearLayout.findViewById(R.id.editTextINSTANCE);
            final EditText editTextUN = (EditText) linearLayout.findViewById(R.id.editTextUN);
            final EditText editTextPASSWORD = (EditText) linearLayout.findViewById(R.id.editTextPASSWORD);

            SharedPreferences prefs = getApplicationContext().getSharedPreferences("SHARED_PREFERENCES", MODE_PRIVATE);
            IP = prefs.getString("IP", IP);//"No name defined" is the default value.
            PORT = prefs.getString("PORT", PORT);//"No name defined" is the default value.
            DB = prefs.getString("DB", DB);//"No name defined" is the default value.
            DBTPVR = prefs.getString("DBTPVR", DBTPVR);//"No name defined" is the default value.
            INSTANCE = prefs.getString("INSTANCE", INSTANCE);//"No name defined" is the default value.
            UN = prefs.getString("UN", UN);//"No name defined" is the default value.
            PASSWORD = prefs.getString("PASSWORD", PASSWORD);//"No name defined" is the default value.
            if (IP != null) {
                editTextIP.setText(IP);
            }
            if (PORT != null) {
                editTextPORT.setText(PORT);
            }
            if (DB != null) {
                editTextDB.setText(DB);
            }
            if (INSTANCE != null) {
                editTextINSTANCE.setText(INSTANCE);
            }
            if (UN != null) {
                editTextUN.setText(UN);
            }
            if (PASSWORD != null) {
                editTextPASSWORD.setText(PASSWORD);
            }
            if (DBTPVR != null) {
                editTextDBRest.setText(DBTPVR);
            }

            Button buttonCancelar = (Button) linearLayout.findViewById(R.id.buttonCancelar);
            Button buttonAcceptar = (Button) linearLayout.findViewById(R.id.buttonAcceptar);

            buttonCancelar.setOnClickListener(
                    new View.OnClickListener() {
                        public void onClick(View view) {
                            pw.dismiss();
                        }
                    });

            buttonAcceptar.setOnClickListener(
                    new View.OnClickListener() {
                        public void onClick(View view) {

                            final String previousIP = IP;
                            final String previousPORT = PORT;
                            final String previousDB = DB;
                            final String previousINSTANCE = INSTANCE;
                            final String previousUN = UN;
                            final String previousPASSWORD = PASSWORD;
                            final String previousTPVR = DBTPVR;

                            IP = editTextIP.getText().toString();
                            PORT = editTextPORT.getText().toString();
                            DB = editTextDB.getText().toString();
                            if (editTextINSTANCE.getText().toString().equals("")) {
                                INSTANCE = null;
                            } else {
                                INSTANCE = editTextINSTANCE.getText().toString();
                            }
                            UN = editTextUN.getText().toString();
                            PASSWORD = editTextPASSWORD.getText().toString();

                            // MY_PREFS_NAME - a static String variable like:
                            // public static final String MY_PREFS_NAME = "MyPrefsFile";
                            SharedPreferences.Editor editor = getSharedPreferences("SHARED_PREFERENCES", MODE_PRIVATE).edit();
                            editor.putString("IP", IP);
                            editor.putString("PORT", PORT);
                            editor.putString("DB", DB);
                            editor.putString("INSTANCE", INSTANCE);
                            editor.putString("UN", UN);
                            editor.putString("PASSWORD", PASSWORD);
                            editor.putString("TPVR", DBTPVR);
                            editor.apply();

                            parametersChanged = true;

                            Handler mHandler = new Handler();
                            mHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    pw.dismiss();

                                    Snackbar.make(findViewById(R.id.coordinatorLayoutMainActivity), "Parametros cambiados", Snackbar.LENGTH_LONG).setAction("Cancelar", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {

                                            IP = previousIP;
                                            PORT = previousPORT;
                                            DB = previousDB;
                                            INSTANCE = previousINSTANCE;
                                            UN = previousUN;
                                            PASSWORD = previousPASSWORD;

                                            editTextIP.setText(IP);
                                            editTextPORT.setText(PORT);
                                            editTextDB.setText(DB);
                                            editTextINSTANCE.setText(INSTANCE);
                                            editTextUN.setText(UN);
                                            editTextPASSWORD.setText(PASSWORD);

                                            SharedPreferences.Editor editor = getSharedPreferences("SHARED_PREFERENCES", MODE_PRIVATE).edit();
                                            editor.putString("IP", IP);
                                            editor.putString("PORT", PORT);
                                            editor.putString("DB", DB);
                                            editor.putString("INSTANCE", INSTANCE);
                                            editor.putString("UN", UN);
                                            editor.putString("PASSWORD", PASSWORD);
                                            editor.apply();

                                            parametersChanged = false;
                                        }
                                    }).show();
                                }
                            }, 300L);

                        }
                    });

            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    pw.showAtLocation(findViewById(R.id.coordinatorLayoutMain), Gravity.CENTER, 0, 0);
                }
            }, 250L);

        }
        else if(id == R.id.nav_activate)
        {
            activateApp activateapp = new activateApp(this, "config.txt");
            activateapp.checkActivation(this);

        }
        else if (id == R.id.nav_tools) {

            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            LinearLayout linearLayout = (LinearLayout) inflater.inflate(R.layout.popup_gestion, null, false);

            CoordinatorLayout coordinatorLayout = findViewById(R.id.coordinatorLayoutMain);
            int widthConstraintLayout = coordinatorLayout.getWidth();
            int heightConstraintLayout = coordinatorLayout.getHeight();

            final PopupWindow pw = new PopupWindow(linearLayout, (int) (widthConstraintLayout * 0.70), (int) (heightConstraintLayout * 0.74), true);

            pw.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                pw.setElevation(10);
            }

            pw.setAnimationStyle(R.style.Animation);

            final EditText editTextCAJA = (EditText) linearLayout.findViewById(R.id.editTextCAJA);
            final EditText editTextALMACEN = (EditText) linearLayout.findViewById(R.id.editTextALMACEN);
            final EditText editTextEMPRESA = (EditText) linearLayout.findViewById(R.id.editTextEMPRESA);
            final EditText editTextCLIENTE = (EditText) linearLayout.findViewById(R.id.editTextCLIENTE);
            final CheckBox chkBoxOffLine = (CheckBox) linearLayout.findViewById(R.id.checkBoxOffline);
            final Spinner spinner = (Spinner) linearLayout.findViewById(R.id.SpinnerMesInicio);


            // Si la versio d'android est supèrieure ou egale a 8 on utlise le mode dropdown pour le spinner
            int spinnerMode = android.R.layout.simple_spinner_item;
            if (spinner != null) {
                if (android.os.Build.VERSION.SDK_INT >= 26) { // >= Oreo
                    // pour les versions 8 et plus le dropdon spinner devrait marcher ----  --- Ça ne fonctionne pas : Reste en mode dialog
                    spinner.setLayoutMode(Spinner.MODE_DROPDOWN);
                    spinnerMode = android.R.layout.simple_spinner_dropdown_item;
                } else {
                    // Sino on pase en mode dialogue --- Ça ne marche pas
                    spinner.setLayoutMode(Spinner.MODE_DIALOG);
                    spinnerMode = android.R.layout.simple_spinner_item;
                }
            }

            SharedPreferences prefs = getApplicationContext().getSharedPreferences("SHARED_PREFERENCES", MODE_PRIVATE);
            CAJA = prefs.getString("CAJA", CAJA);//"No name defined" is the default value.
            ALMACEN = prefs.getString("ALMACEN", ALMACEN);//"No name defined" is the default value.
            EMPRESA = prefs.getString("EMPRESA", EMPRESA);//"No name defined" is the default value.
            CLIENTE = prefs.getString("CLIENTE", CLIENTE);//"No name defined" is the default value.
            OFFLINE = prefs.getBoolean("OFFLINE", OFFLINE);//"No name defined" is the default value.
            if (CAJA != null) {
                editTextCAJA.setText(CAJA);
            }
            if (ALMACEN != null) {
                editTextALMACEN.setText(ALMACEN);
            }
            if (EMPRESA != null) {
                editTextEMPRESA.setText(EMPRESA);
            }
            if (CLIENTE != null) {
                editTextCLIENTE.setText(CLIENTE);
            }

            chkBoxOffLine.setChecked(OFFLINE);
            //pw.setWidth(LinearLayout.LayoutParams.WRAP_CONTENT);
            pw.setHeight(LinearLayout.LayoutParams.WRAP_CONTENT);

            Button buttonCancelar = (Button) linearLayout.findViewById(R.id.buttonCancelarGestion);
            Button buttonAcceptar = (Button) linearLayout.findViewById(R.id.buttonAcceptarGestion);

            recuperarSalas = new RecuperarSalas(m_mainActiviy, spinner, START_ROOM, false, spinnerMode);

            recuperarSalas.execute();


            buttonCancelar.setOnClickListener(
                    new View.OnClickListener() {
                        public void onClick(View view) {
                            pw.dismiss();
                        }
                    });

            buttonAcceptar.setOnClickListener(
                    new View.OnClickListener() {
                        public void onClick(View view) {
                            final String previousCAJA = CAJA;
                            final String previousALMACEN = ALMACEN;
                            final String previousEMPRESA = EMPRESA;
                            final String previousCLIENTE = CLIENTE;
                            final boolean previousOFFLINE = OFFLINE;
                            final String previousSTART_ROOM = START_ROOM;
                            final Situacion situ;

                            CAJA = editTextCAJA.getText().toString();
                            ALMACEN = editTextALMACEN.getText().toString();
                            DB = editTextEMPRESA.getText().toString();
                            UN = editTextCLIENTE.getText().toString();
                            OFFLINE = chkBoxOffLine.isChecked();
                            //Adapter adapter = spinner.getAdapter();
                            //if(adapter != null) {
                            if (spinner.getCount() > 0) {
                                situ = (Situacion) spinner.getSelectedItem();
                                if (situ != null)
                                    START_ROOM = situ.getDescription();
                            }
                            //}

                            SharedPreferences.Editor editor = getSharedPreferences("SHARED_PREFERENCES", MODE_PRIVATE).edit();
                            editor.putString("CAJA", CAJA);
                            editor.putString("ALMACEN", ALMACEN);
                            editor.putString("EMPRESA", EMPRESA);
                            editor.putString("CLIENTE", CLIENTE);
                            editor.putBoolean("OFFLINE", OFFLINE);
                            editor.putString("START_ROOM", START_ROOM);

                            editor.apply();

                            Handler mHandler = new Handler();
                            mHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    pw.dismiss();
                                    Snackbar.make(findViewById(R.id.coordinatorLayoutMainActivity), getResources().getString(R.string.parametros_guardados), Snackbar.LENGTH_LONG).setAction("Cancelar", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            CAJA = previousCAJA;
                                            ALMACEN = previousALMACEN;
                                            EMPRESA = previousEMPRESA;
                                            CLIENTE = previousCLIENTE;
                                            OFFLINE = previousOFFLINE;
                                            START_ROOM = previousSTART_ROOM;

                                            editTextCAJA.setText(CAJA);
                                            editTextALMACEN.setText(ALMACEN);
                                            editTextEMPRESA.setText(EMPRESA);
                                            editTextCLIENTE.setText(UN);
                                            chkBoxOffLine.setChecked(OFFLINE);


                                            SharedPreferences.Editor editor = getSharedPreferences("SHARED_PREFERENCES", MODE_PRIVATE).edit();
                                            editor.putString("CAJA", CAJA);
                                            editor.putString("ALMACEN", ALMACEN);
                                            editor.putString("EMPRESA", EMPRESA);
                                            editor.putString("CLIENTE", CLIENTE);
                                            editor.putString("START_ROOM", START_ROOM);
                                            editor.putBoolean("OFFLINE", OFFLINE);
                                            //editor.apply();
                                            //refrescaMesas();
                                            //pullToRefresh.setRefreshing(false);
                                            loadActivity();
                                        }
                                    }).show();
                                }
                            }, 300L);


                            if (m_spinnerSalas != null) {
                                //Adapter spadapter = m_spinnerSalas.getAdapter();
                                //if(spadapter != null) {
                                Situacion situacion;
                                for (int i = 0; i < m_spinnerSalas.getCount(); ++i) {
                                    situacion = (Situacion) m_spinnerSalas.getItemAtPosition(i);

                                    if (situacion.getDescription().equals(START_ROOM)) {
                                        m_spinnerSalas.setSelection(i);
                                        break;
                                    }
                                }
                                //}
                            }
                            // Il faut rafraichir les sales , les tables et la con nection donc on appele acrrément
                            // La procedure appelée par on create
                            //refrescaMesas();
                            loadActivity();
                            pullToRefresh.setRefreshing(false);
                        }
                    });

            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    pw.showAtLocation(findViewById(R.id.coordinatorLayoutMain), Gravity.CENTER, 0, 0);
                }
            }, 250L);

        } else if (id == R.id.nav_sync) { // Synchroniser la base de donnée

            Intent intent = new Intent(this, SyncDatabase.class);
            this.startActivity(intent);

        } else if (id == R.id.nav_about) {

            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            LinearLayout linearLayout = (LinearLayout) inflater.inflate(R.layout.popup_about, null, false);

            CoordinatorLayout coordinatorLayout = findViewById(R.id.coordinatorLayoutMain);
            int widthConstraintLayout = coordinatorLayout.getWidth();
            int heightConstraintLayout = coordinatorLayout.getHeight();

            final PopupWindow pw = new PopupWindow(linearLayout, (int) (widthConstraintLayout * 0.70), (int) (heightConstraintLayout * 0.30), true);

            pw.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                pw.setElevation(10);
            }

            pw.setAnimationStyle(R.style.Animation);
            pw.setWidth(LinearLayout.LayoutParams.WRAP_CONTENT);
            pw.setHeight(LinearLayout.LayoutParams.WRAP_CONTENT);
            pw.setFocusable(true);


            TextView tv = linearLayout.findViewById(R.id.textViewConnecting);
            ;
            if (tv != null)
                tv.setText(BuildConfig.APP_DISPLAY_NAME);

            ImageView iv = linearLayout.findViewById(R.id.imageViewLogo);
            if (iv != null)
                iv.setImageResource(BuildConfig.ABOUT_BOX_LOGO);


            tv = linearLayout.findViewById(R.id.textViewInfo);
            ;
            if (tv != null)
                tv.setText(String.format("V %s", BuildConfig.VERSION_NAME));


            tv = linearLayout.findViewById(R.id.textViewInfoComment);
            if (tv != null)
                tv.setText(BuildConfig.ABOUT_BOX_STRING);

            Button buttonCancelar = linearLayout.findViewById(R.id.btnSalir);
            if (buttonCancelar != null)
                buttonCancelar.setOnClickListener(
                        new View.OnClickListener() {
                            public void onClick(View view) {
                                pw.dismiss();
                            }
                        });


            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    pw.showAtLocation(findViewById(R.id.coordinatorLayoutMain), Gravity.CENTER, 0, 0);
                }
            }, 250L);
        }/* else if (id == R.id.nav_send) {

        }*/

        return true;
    }

    public NavigationView getNavigationView() {
        return this.navigationView;
    }

    public void setCamarero(String camarero) {
        this.m_camarero = camarero;
    }

    public String getCamarero() {
        return this.m_camarero;
    }

    public void dismissBottomSheetDialog() {
        if (mBottomSheetDialog != null && mBottomSheetDialog.isShowing())
            mBottomSheetDialog.dismiss();
    }

    ;

    private void showBottomSheetDialog() {

        View view = getLayoutInflater().inflate(R.layout.bottomsheet_productos, null);
        //RecyclerView recyclerViewMesas = (RecyclerView) view.findViewById(R.id.recyclerViewMesas);
        camarerosRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);

        camareros = new ArrayList<>();
        camareros.add("Gandalf");
        camareros.add("Yoda");
        camareros.add("Dumbledore");

        camarerosRecyclerView.setHasFixedSize(true);
        camarerosRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        camarerosRecyclerView.setAdapter(new CamarerosAdapter(camareros, this));

        mBottomSheetDialog.setContentView(view);
        mBottomSheetDialog.show();

        mBottomSheetDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                mBottomSheetDialog = null;
            }
        });


    }

    public class RecuperaCamareros extends AsyncTask<String, String, String> {
        String z = "";
        Boolean isSuccess = true;
        SQLiteTPVR mSqlite = new SQLiteTPVR(m_context);
        Usuario usuario;
        boolean m_forceChoose = false;


        public RecuperaCamareros(boolean force) {
            m_forceChoose = force;
        }

        @Override
        protected void onPreExecute() {

            // Clear the list
            Log.d("List", "Cleaning the list");
            camareros = new ArrayList<String>();
            camareros.clear();

        }

        @Override
        protected void onPostExecute(String r) {
            //pbbar.setVisibility(View.GONE);

            if (isSuccess) {

                if (m_camarero.isEmpty() || m_forceChoose) {
                    mBottomSheetDialog = new BottomSheetDialog(m_context);
                    View view = getLayoutInflater().inflate(R.layout.bottomsheet_productos, null);
                    //RecyclerView recyclerViewMesas = (RecyclerView) view.findViewById(R.id.recyclerViewMesas);
                    camarerosRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);

                    camarerosRecyclerView.setHasFixedSize(true);
                    camarerosRecyclerView.setLayoutManager(new LinearLayoutManager(m_context));
                    camarerosRecyclerView.setAdapter(new CamarerosAdapter(camareros, m_mainActiviy));

                    mBottomSheetDialog.setContentView(view);
                    mBottomSheetDialog.show();
                    mBottomSheetDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            mBottomSheetDialog = null;
                        }
                    });
                }


            } else {
                if(z.length() >0 )
                        AlertToast(z);
            }

        }

        @Override
        protected String doInBackground(String... params) {


            //initializing the waites list
            try {

                if (mSqlite != null) {
                    SQLiteDatabase db = mSqlite.getReadableDatabase();
                    Log.d("Rec camareros", "SQLITE server Connection ok");

                    if (db != null) {
                        Log.d("Rec camareros", "SQLITE db Connection ok");
                        Cursor cursor = db.query("Usuarios", null, null,
                                null, null, null, null, null);
                        if (cursor != null) {
                            Log.d("Rec camareros", "SQLITE Connection ok");
                            if (cursor.moveToFirst()) {
                                do {
                                    usuario = mSqlite.getUsuario(cursor);
                                    camareros.add(usuario.getName());
                                } while (cursor.moveToNext());
                            } else {
                                isSuccess = false;
                                z = "No record found";
                            }
                            cursor.close();
                        } else {
                            isSuccess = false;
                            z = "Can't readTable";
                        }
                        db.close();
                    } else {
                        isSuccess = false;
                        z = "Can't open database";
                    }
                } else {

                    isSuccess = false;
                    z = "Can't connect to server";
                }

            } catch (Exception ex) {
                isSuccess = false;
                z = "Exception: " + ex.getMessage();

                Log.e("ERROR GET CAMAREROS:", ex.getMessage());
            }


            return z;
        }
    }

    public class RecuperarSalas extends AsyncTask<String, String, String> {
        Boolean isSuccess = false;
        Spinner spinnerSalas = null;
        String sala = null;
        boolean m_getTablesToo = true;
        Activity m_activity;
        ArrayList<Situacion> salas = new ArrayList<>();
        String error = "";
        byte[] blob;
        Bitmap bmp;
        SalasAdapter salasAdapter;
        int m_spinner_mode; // Dropdown ou Dialog


        RecuperarSalas(Activity activity, Spinner spinner, String sala) {
            this.spinnerSalas = spinner;
            this.sala = sala;
            m_getTablesToo = true;
            this.m_activity = activity;
            m_spinner_mode = android.R.layout.simple_spinner_dropdown_item;
        }

        RecuperarSalas(Activity activity, Spinner spinner, String sala, boolean tablesToo) {
            this.spinnerSalas = spinner;
            this.sala = sala;
            m_getTablesToo = tablesToo;
            this.m_activity = activity;
            m_spinner_mode = android.R.layout.simple_spinner_dropdown_item;
        }

        RecuperarSalas(Activity activity, Spinner spinner, String sala, boolean tablesToo, int spinner_mode) {
            this.spinnerSalas = spinner;
            this.sala = sala;
            m_getTablesToo = tablesToo;
            this.m_activity = activity;
            m_spinner_mode = spinner_mode;
        }


        @Override
        protected void onPreExecute() {
            salas.clear();
        }

        @Override
        protected void onPostExecute(String r) {

            if (isSuccess) {
                /*ArrayAdapter<Situacion> adapter = new ArrayAdapter<Situacion>(
                        m_context,
                        android.R.layout.simple_spinner_dropdown_item,
                        salas
                );*/

                /* Version avec des images*/
                salasAdapter = new SalasAdapter(m_context, m_spinner_mode, salas);
                spinnerSalas.setAdapter(salasAdapter);


                /* Version seulement texte mais avec des objets */
               /* ArrayAdapter<Situacion> adapter = new ArrayAdapter<Situacion>(
                        m_context,
                        android.R.layout.simple_spinner_dropdown_item,
                        salas
                );
                adapter.setDropDownViewResource(m_spinner_mode);
                spinnerSalas.setAdapter(adapter);*/


                //adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);


                if (spinnerSalas.getCount() > 0) {
                    if (this.sala != null)
                        setSpinnerItemSelected(spinnerSalas, this.sala);
                    //spinnerSalas.setSelection(intemPosition);
                    m_situacion = (Situacion) spinnerSalas.getSelectedItem();
                    // m_sala = m_situacion.getDescription();

                    // Ahora se recuperan las mesas
                    if (m_getTablesToo) {
                        Log.d("Sala ", "Recuperando las mesas de la sala" + m_situacion.getDescription());
                        recyclerViewMesas = (RecyclerView) findViewById(R.id.recyclerView);
                        recyclerViewMesas.setHasFixedSize(true);
                        recyclerViewMesas.setLayoutManager(new GridLayoutManager(m_context, 4));
                        showConnecting(R.string.connecting);
                        recuperarMesas = new RecuperarMesas(m_mainActiviy, recyclerViewMesas);
                        //recuperarMesas = new RecuperarMesas(recyclerViewMesas);
                        recuperarMesas.execute();
                    }
                }
            } else {
                AlertToast(error);
            }

        }

        @Override
        protected String doInBackground(String... params) {

            DatabaseManipulation dbmap;

            if (OFFLINE) {
                dbmap = new SQLiteManipulation(m_mainActiviy, DBTPVR);
            } else {
                dbmap = new SQLServerManipulation(m_mainActiviy, DBTPVR);
            }

            //salas = dbmap.retrieveRooms();


            try {

                if (dbmap == null && dbmap.isConnected()) {
                    error = "Error in connection with database server";
                } else {
                    String query = "SELECT Clave\n" +
                            "      ,Descripcion\n" +
                            "      ,Mesas\n" +
                            "      ,Tarifa\n" +
                            "      ,Img\n" +
                            "      ,PrimeraMesa\n" +
                            "      ,Impresora\n" +
                            "      ,Driver\n" +
                            "      ,Port\n" +
                            "  FROM Situacion";


                    Log.d("Query", query);
                    try {

                        salas.clear();
                        //Statement stmt = con.createStatement();
                        try {
                            //ResultSet rs = stmt.executeQuery(query);
                            isSuccess = true;
                            dbmap.rawReadQuery(query);
                            if (dbmap.first()) {
                                do {


                                    // Si ce n'est pas un bar ou que ve n'est pas la version Diseny
                                    //if (!BuildConfig.BUILD_VERSION.equals("DISENY") || dbmap.getCursorInt(3) > 0) {
                                    Log.d("Añadiendo sala", dbmap.getCursorString(2));
                                    //salas.add(dbmap.getCursorString("Descripcion"));
                                    blob = dbmap.getCursorBlob(5);
                                    if (blob != null && blob.length > 0) {
                                        bmp = BitmapFactory.decodeByteArray(blob, 0, blob.length);
                                    } else {
                                        int mesas = dbmap.getCursorInt(3);
                                        if (mesas < 1) {
                                            bmp = BitmapFactory.decodeResource(m_activity.getResources(), R.drawable.barra64);
                                        } else {
                                            bmp = BitmapFactory.decodeResource(m_activity.getResources(), R.drawable.dining_room_64);
                                        }
                                    }

                                    salas.add(new Situacion(dbmap.getCursorString(1) // clave
                                            , dbmap.getCursorString(2) // Description
                                            , dbmap.getCursorInt(3) // Mesas
                                            , dbmap.getCursorString(4) // Tarifa
                                            , dbmap.getCursorString(6) // Primera mesa
                                            , dbmap.getCursorString(7) // Impresora
                                            , dbmap.getCursorString(8) // Driver
                                            , dbmap.getCursorString(9) // Port
                                            , bmp)

                                    );
                                } while (dbmap.next());

                           /* if (sala != null && sala.equals(dbmap.getCursorString(2)) == true) {
                                intemPosition = position;
                            }
                            ++position;*/
                                //}
                            }

                        } catch (Exception ex) {
                            error = "Exception: " + ex.getMessage();
                            Log.d("ER/Stmt add situacion", error);
                        }

                    } catch (Exception ex) {
                        error = "Exception: " + ex.getMessage();
                        Log.d("ER/Stmt open situ", error);
                    }


                }
            } catch (Exception ex) {
                isSuccess = false;
                error = "Exception: " + ex.getMessage();

                Log.e("ERROR:", ex.getMessage());
            } finally {
                if (dbmap != null)
                    dbmap.close();
            }


            if (salas != null && salas.size() > 0) {
                isSuccess = true;
            } else {
                error = "Could no retrieve rooms";
            }
            return error;
        }


    }


    public class RecuperarMesas extends AsyncTask<String, String, String> {
        String error = "";
        Boolean isSuccess = false;
        ArrayList<String> salas = new ArrayList<String>();
        List<Mesa> mesaList = new ArrayList<>();
        List<Mesa> mesabarra = new ArrayList<>();
        Mesa mesa;
        HashMap<String, Mesa> mesaState = new LinkedHashMap<>();
        HashMap<String, Mesa> mesasUnidas = new LinkedHashMap<>();

        String estado, ticket;
        int unida = 0;
        RecyclerView m_recyclerView = null;
        String query;
        int mesaSateSize = 0;
        Activity m_activity;


        public RecuperarMesas(Activity activity) {
            m_recyclerView = recyclerViewMesas;
            m_activity = activity;
        }

        public RecuperarMesas(Activity activity, RecyclerView rec) {
            m_recyclerView = rec;
            m_activity = activity;
        }


        @Override
        protected void onPreExecute() {
            mesaList.clear();
            mesaState.clear();
            mesabarra.clear();
            isSuccess = false;
            m_spinnerSalas.getSelectedItem();
        }

        @Override
        protected void onPostExecute(String r) {
            // Spinner spinnerSalas = findViewById(R.id.spinner);
            hideConnecting();
            if (isSuccess) {

                //creating recyclerview adapter
                MesasAdapter mesasAdapter = new MesasAdapter(MainActivity.this, mesaList, m_camarero);

                //setting adapter to recyclerview
                m_recyclerView.setAdapter(mesasAdapter);

            } else {
                // Erreur don on remet le spinner a la sale de départ
                if (m_prev_situacion != null)
                    m_situacion = m_prev_situacion;

                dontReloadTables = true; // On ne veut pas qu'au changement de selection du spinner il recharge les tables
                if(m_situacion != null)
                     setSpinnerItemSelected(m_spinnerSalas, m_situacion.getDescription());

                // On arrête la selection de tables pour move ou join
                if (mesasSeleccionadas.size() > 0) {

                    if (enviandoMesa) {
                        msgSnackBar.dismiss();
                        enviandoMesa = false;
                    }

                    mesasSeleccionadas.clear();
                    if (isFABOpen) {
                        closeFABMenu(true);
                    }
                    Handler mHandler = new Handler();
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            fab.hide();
                        }
                    }, 400L);
                    recyclerViewMesas.getAdapter().notifyItemRangeChanged(0, recyclerViewMesas.getAdapter().getItemCount());
                    //refrescaMesas();

                }
                if(error.length()>0)
                     AlertToast(error);
            }
        }

        @Override
        protected String doInBackground(String... params) {

            DatabaseManipulation dbmap;


            try {

                if (OFFLINE) {
                    dbmap = new SQLiteManipulation(m_mainActiviy, DBTPVR);
                    Log.d("MODE ", "OFFLINE");
                } else {
                    dbmap = new SQLServerManipulation(m_mainActiviy, DBTPVR);
                    if (!dbmap.isConnected()) {
                        // Si le serveur n'est pa dispo
                        dbmap = null;
                        error = getResources().getString(R.string.error_data_base_connection);
                        isSuccess = false;

                    }
                    Log.d("MODE ", "ONLINE");
                }

                    /*if (con == null) {
                        z = "Error in connection with SQL server";
                    } else {*/


                try {
                    if (dbmap != null && m_situacion != null) {
                        Log.d("Tarifa", m_situacion.getTarifa());
                        Log.d("Tables: ", String.valueOf(m_situacion.getMesas()));
                        Log.d("Première table: ", String.valueOf(m_situacion.getPrimeraMesa()));
                        if (m_situacion.getMesas() < 1) { // Si es el bar no hay mesa luego siempre esta disponible

                            Bitmap bmp;
                            bmp = m_situacion.getBitmap();
                            if (bmp == null) {
                                bmp = BitmapFactory.decodeResource(m_activity.getResources(), R.drawable.barra64);
                            }

                            mesaList.add(


                                    new Mesa(
                                            1,
                                            "",
                                            m_situacion.getDescription(),
                                            m_situacion.getClave(),
                                            m_situacion.getDescription(),
                                            m_situacion.getTarifa(),
                                            "",
                                            "",
                                            bmp, true));
                        }
                        //else {

                        // On récupère les tables qui ont une commande en cours quel que soit l'état
                        // pour la couleur d'affichage d ela table
                        query = "SELECT distinct \n" +
                                "      Situ\n" +
                                "      ,Mesa\n" +
                                "      ,Estado\n" +
                                "      ,Nticket\n" +
                                "  FROM TicketsHist\n" +
                                "  WHERE (Estado <> '' AND Estado <> 'A' AND Estado <> 'J') \n" +
                                "  AND Situ = '" + m_situacion.getClave() + "'";

                        Log.d("Query", query);
                        try {
                            dbmap.rawReadQuery(query);


                            if (dbmap.first()) {

                                do {
                                    ++mesaSateSize;

                                    mesabarra.add(
                                            new Mesa(
                                                    1,
                                                    "",
                                                    dbmap.getCursorString(2).trim(), // Nº de la table dns shortdesc pour les tables de la "barra"
                                                    m_situacion.getClave(),
                                                    m_situacion.getDescription(),
                                                    m_situacion.getTarifa(),
                                                    dbmap.getCursorString(3), // Etat de l atable
                                                    dbmap.getCursorString(4), // Nº d eticket
                                                    BitmapFactory.decodeResource(m_activity.getResources(), R.drawable.cliente_barra64),
                                                    true
                                            ));
                                    String estado = dbmap.getCursorString(3);

                                    mesaState.put(dbmap.getCursorString(2).trim(), new Mesa(
                                            1,
                                            dbmap.getCursorString(2).trim(),
                                            "Ocupada",
                                            m_situacion.getClave(),
                                            m_situacion.getDescription(),
                                            m_situacion.getTarifa(),
                                            estado,
                                            dbmap.getCursorString(4),
                                            BitmapFactory.decodeResource(m_activity.getResources(), R.drawable.mesa_128),
                                            //(estado.equals("P") || estado.equals("C")) ? BitmapFactory.decodeResource(m_activity.getResources(),R.drawable.ic_mesa_ocupada) : estado.equals("E") ? BitmapFactory.decodeResource(m_activity.getResources(),R.drawable.ic_mesa_ocupada) :BitmapFactory.decodeResource(m_activity.getResources(), R.drawable.ic_mesa),
                                            true
                                    ));

                                    Log.d(dbmap.getCursorString(1), dbmap.getCursorString(2));
                                } while (dbmap.next());
                            }


                        } catch (Exception ex) {
                            error = "Exception: " + ex.getMessage();
                            Log.d("ER/Tables state", error);
                        }

                        // Recuperer les tables regroupés
                        query = "SELECT NTicket\n" +
                                "      ,Estado\n" +
                                "      ,Situ\n" +
                                "      ,Mesa\n" +
                                "  FROM TicketsHist\n" +
                                "  WHERE Estado = 'J' AND Situ = '" + m_situacion.getClave() + "' ";

                        Log.d("Query", query);

                        try {
                            dbmap.rawReadQuery(query);
                            Mesa mesa;
                            String estado;
                            // Recuperation des tables qui csont unies dans la sale
                            if (dbmap.first()) {
                                do {
                                    // On récupère l'etat de la table principale pour l'affecter aux autres tables
                                    Log.d("Récup état table nº", String.format(Locale.FRANCE, "%d", dbmap.getCursorInt(1)));
                                    mesa = mesaState.get(dbmap.getCursorInt(1));// Le numero de la table se trouve dans la colonne ticket
                                    if (mesa != null) {
                                        estado = mesa.getState();
                                        Log.d("Récup estado", String.format(Locale.FRANCE, "mesa not null %s", estado));
                                    } else {
                                        estado = dbmap.getCursorString(2);
                                        Log.d("Récup estado", "mesa null");
                                    }
                                    mesasUnidas.put(dbmap.getCursorString(4).trim(), new Mesa(
                                            1,
                                            dbmap.getCursorString(4), // Table
                                            "Ocupada",
                                            m_situacion.getClave(),
                                            m_situacion.getDescription(),
                                            m_situacion.getTarifa(),
                                            estado,
                                            dbmap.getCursorString(1), // Nº Ticket
                                            BitmapFactory.decodeResource(m_activity.getResources(), R.drawable.mesa_128),
                                            //(estado.equals("P") || estado.equals("C")) ? BitmapFactory.decodeResource(m_activity.getResources(),R.drawable.ic_mesa_ocupada) : estado.equals("E") ? BitmapFactory.decodeResource(m_activity.getResources(),R.drawable.ic_mesa_ocupada) : BitmapFactory.decodeResource(m_activity.getResources(),R.drawable.ic_mesa),
                                            dbmap.getCursorInt(1)
                                    ));

                                    Log.d(dbmap.getCursorString(3), dbmap.getCursorString(4));
                                } while (dbmap.next());
                            }
                            isSuccess = true;


                        } catch (Exception ex) {
                            error = "Exception: " + ex.getMessage();
                            Log.d("ER/Tables state", error);
                        } finally {
                            dbmap.close();
                        }

                        if (m_situacion.getMesas() > 0) {
                            for (int i = 0; i < m_situacion.getMesas(); ++i) {
                                Log.d("Ajout de table", String.valueOf(i + m_situacion.getPrimeraMesa()));

                                // Verificar si la mesa esta en la lista
                                mesa = mesaState.get(String.format(Locale.FRANCE, "%d", i + m_situacion.getPrimeraMesa()));
                                if (mesa != null) {
                                    estado = mesa.getState();
                                    ticket = mesa.getTicket();
                                } else {
                                    estado = "";
                                    ticket = "";
                                }

                                mesa = mesasUnidas.get(String.format(Locale.FRANCE, "%d", i + m_situacion.getPrimeraMesa()));
                                if (mesa != null) {

                                    unida = mesa.getLinkedTable();
                                    Mesa table = mesaState.get(String.format(Locale.FRANCE, "%d", unida));
                                    if (table != null)
                                        estado = table.getState();
                                    else estado = "";
                                } else {
                                    unida = 0;
                                }
                                Drawable drawable = ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_mesa);


                                //Bitmap bmp = (estado.equals("P") || estado.equals("C")) ? BitmapFactory.decodeResource(m_activity.getResources(),R.drawable.ic_mesa_ocupada) : estado.equals("E") ? BitmapFactory.decodeResource(m_activity.getResources(),R.drawable.ic_mesa_ocupada) : BitmapFactory.decodeResource(m_activity.getResources(),R.drawable.cliente_barra64);
                                //Bitmap bmp = Bitmap.createBitmap(drawable);
                                mesaList.add(
                                        new Mesa(
                                                1,
                                                String.format(Locale.FRANCE, "%d", i + m_situacion.getPrimeraMesa()),
                                                "", //       (mesa != null) ? "Ocupada" : "libre",
                                                m_situacion.getClave(),
                                                m_situacion.getDescription(),
                                                m_situacion.getTarifa(),
                                                estado,
                                                ticket,
                                                BitmapFactory.decodeResource(m_activity.getResources(), R.drawable.mesa_128),
                                                unida
                                        ));
                            }
                        } else {

                            mesaList.addAll(mesabarra);
                        }
                    }
                    //}


                    //}
                } catch (Exception ex) {
                    error = "Exception: " + ex.getMessage();
                    Log.d("ER/Stmt", error);
                }
                        /*} catch (Exception ex) {
                            z = "Exception: " + ex.getMessage();
                            Log.d("ER/Stmt", z);
                        }*/

                //}

            } catch (Exception ex) {
                isSuccess = false;
                error = "Exception: " + ex.getMessage();

                Log.e("ERROR:", ex.getMessage());
            }

            //}


            return error;
        }


    }

    public SQLServerManipulation checkSqlServerConnection() {
        SQLServerManipulation dbmap;
        // Afficher un message d'info


        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LinearLayout linearLayout = (LinearLayout) inflater.inflate(R.layout.popup_info, null, false);

        CoordinatorLayout coordinatorLayout = findViewById(R.id.coordinatorLayoutMain);
        int widthConstraintLayout = coordinatorLayout.getWidth();
        int heightConstraintLayout = coordinatorLayout.getHeight();

        final PopupWindow pw = new PopupWindow(linearLayout, (int) (widthConstraintLayout * 0.70), (int) (heightConstraintLayout * 0.30), true);

        pw.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            pw.setElevation(10);
        }

        pw.setAnimationStyle(R.style.Animation);
        pw.setWidth(LinearLayout.LayoutParams.WRAP_CONTENT);
        pw.setHeight(LinearLayout.LayoutParams.WRAP_CONTENT);
        pw.setFocusable(true);


        TextView tv = linearLayout.findViewById(R.id.textViewInfoTitle);

        if (tv != null)
            tv.setText(R.string.ic_connecting_database);


        tv = linearLayout.findViewById(R.id.textViewInfoComment);

        if (tv != null) {
            String text = getString(R.string.ic_connecting_database) + ".\n" + getString(R.string.ic_please_wait);
            tv.setText(text);
        }

        Handler mHandler = new Handler();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                pw.showAtLocation(findViewById(R.id.coordinatorLayoutMain), Gravity.CENTER, 0, 0);
            }
        }, 250L);
        //pw.showAtLocation(findViewById(R.id.coordinatorLayoutMain), Gravity.CENTER, 0, 0);

        dbmap = new SQLServerManipulation(m_mainActiviy, DBTPVR);
        // Enlever le message d'info un message d'info
        pw.dismiss();

        return dbmap;
    }


}

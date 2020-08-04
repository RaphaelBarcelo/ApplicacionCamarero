package com.example.applicacioncamarero.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.applicacioncamarero.R;
import com.example.applicacioncamarero.connexion.ConnectionClass;
import com.example.applicacioncamarero.connexion.SQLiteTPVR;
import com.example.applicacioncamarero.dataClasses.Arqueo;
import com.example.applicacioncamarero.dataClasses.Articulos;
import com.example.applicacioncamarero.dataClasses.Comentarios;
import com.example.applicacioncamarero.dataClasses.ConfTicket;
import com.example.applicacioncamarero.dataClasses.Departamentos;
import com.example.applicacioncamarero.dataClasses.Situacion;
import com.example.applicacioncamarero.dataClasses.SubFamilias;
import com.example.applicacioncamarero.dataClasses.Tarifas;
import com.example.applicacioncamarero.dataClasses.TarifasSubFamilias;
import com.example.applicacioncamarero.dataClasses.TiposIva;
import com.example.applicacioncamarero.dataClasses.Usuario;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.TimeZone;

public class SyncDatabase extends AppCompatActivity {

    //private SQLiteTPVR m_sqlite;
    private Context m_context = this;
    private DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.FRANCE);
    private int m_nbProcesses =0;
    private String m_caja="01";
    private String m_almacen = "01";
    private String m_empresa = "01";
    String DB = "Di00001";
    String DBTPVR = "TPVR";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TimeZone timezone =  TimeZone.getDefault();
        df.setTimeZone(timezone);
        Log.d("Default timezone",timezone.getID());

        setContentView(R.layout.activity_sync_database);
        setTitle(R.string.sincronizar);
        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); // Make to run your application only in portrait mode
        resetProgressBars();

        SharedPreferences prefs = getApplicationContext().getSharedPreferences("SHARED_PREFERENCES", MODE_PRIVATE);
        m_caja = prefs.getString("CAJA", m_caja);//"No name defined" is the default value.
        m_almacen = prefs.getString("ALMACEN", m_almacen);//"No name defined" is the default value.
        m_empresa = prefs.getString("EMPRESA", m_empresa);//"No name defined" is the default value.
        DB = prefs.getString("DB", DB);//"No name defined" is the default value.
        DBTPVR = prefs.getString("DBTPVR", DBTPVR);//"No name defined" is the default value.

        ((Button) findViewById(R.id.btn_run)).setEnabled(false);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if(newConfig.orientation == Configuration.ORIENTATION_PORTRAIT)
        {

        }
        else
        {}
    }

    public void resetProgressBars()
    {
        ProgressBar progressbar;
        TextView tv;


        // Initialising progressbars and percentage textboxes
        progressbar = findViewById(R.id.progressBarUsuarios);
        progressbar.setProgress(0);

        tv = findViewById(R.id.textViewPCUsuarios);
        tv.setText("0%");

        progressbar = findViewById(R.id.progressBarUbicaciones);
        progressbar.setProgress(0);

        tv = findViewById(R.id.textViewPCUbicaciones);
        tv.setText("0%");

        progressbar = findViewById(R.id.progressBarDepartamentos);
        progressbar.setProgress(0);

        tv = findViewById(R.id.textViewPCDepartamentos);
        tv.setText("0%");

        progressbar = findViewById(R.id.progressBarProductos);
        progressbar.setProgress(0);

        tv = findViewById(R.id.textViewPCProductos);
        tv.setText("0%");


        progressbar = findViewById(R.id.progressBarSubfamilias);
        progressbar.setProgress(0);

        tv = findViewById(R.id.textViewPCSubfamilias);
        tv.setText("0%");


        progressbar = findViewById(R.id.progressBarTarifas);
        progressbar.setProgress(0);

        tv = findViewById(R.id.textViewPCTarifas);
        tv.setText("0%");


        progressbar = findViewById(R.id.progressBarTarifasSub);
        progressbar.setProgress(0);

        tv = findViewById(R.id.textViewPCTarifasSub);
        tv.setText("0%");

        progressbar = findViewById(R.id.progressBarComentarios);
        progressbar.setProgress(0);

        tv = findViewById(R.id.textViewPCComentarios);
        tv.setText("0%");


        progressbar = findViewById(R.id.progressBarIva);
        progressbar.setProgress(0);

        tv = findViewById(R.id.textViewPCIva);
        tv.setText("0%");
    }

    public void DecNbProcesses()
    {
        --m_nbProcesses;
        if(m_nbProcesses <=0)
        {
            Button btn= (Button) findViewById(R.id.btn_run);

            if(btn != null) {
                btn.setEnabled(true);
                btn.setVisibility(View.VISIBLE);
            }
            m_nbProcesses =0;
        }

    }

    public void importDatabase(View v)
    {

        CheckBox cb;
        //Log.d("Importing","Entered...");
        resetProgressBars();
        int nbSync =0;

        m_nbProcesses =0;

        Button btn= (Button) findViewById(R.id.btn_run);

        if(btn != null) {
            btn.setEnabled(false);
            //btn.setVisibility(View.INVISIBLE);
        }


        cb = findViewById(R.id.checkBoxUsuarios);
        if(cb.isChecked()) {
            new ImportarUsuarios().execute(50);
            ++nbSync;
            ++m_nbProcesses;
        }
        cb = findViewById(R.id.checkBoxUbicaciones);
        if(cb.isChecked()) {
            new ImportarSituacion().execute(50);
            ++nbSync;
            ++m_nbProcesses;
        }
        cb = findViewById(R.id.checkBoxDepartamentos);
        if(cb.isChecked()) {
            new ImportarDepartamentos().execute(50);
            ++nbSync;
            ++m_nbProcesses;
        }
        cb = findViewById(R.id.checkBoxProductos);
        if(cb.isChecked()) {
            new ImportarProductos().execute(50);
            ++nbSync;
            ++m_nbProcesses;
        }
        cb = findViewById(R.id.checkBoxSubFamilias);
        if(cb.isChecked()) {
            new ImportarSubfamilias().execute(50);
            ++nbSync;
            ++m_nbProcesses;
        }
        cb = findViewById(R.id.checkBoxTarifas);
        if(cb.isChecked()) {
            new ImportarTarifas().execute(50);
            ++nbSync;
            ++m_nbProcesses;
        }


        cb = findViewById(R.id.checkBoxTarifasSub);
        if(cb.isChecked()) {
            new ImportarTarifasSubFamilia().execute(50);
            ++nbSync;
            ++m_nbProcesses;
        }


        cb = findViewById(R.id.checkBoxComentarios);
        if(cb.isChecked()) {
            new ImportarComentarios().execute(50);
            ++nbSync;
            ++m_nbProcesses;
        }

        cb = findViewById(R.id.checkBoxIva);
        if(cb.isChecked()) {
            new ImportarIva().execute(50);
            ++nbSync;
            ++m_nbProcesses;
        }

        // Importar las bases locales

        new ImportarArqueo().execute();

        new ImportarConfTicket().execute();




        if(nbSync == 0)
        {
            displayWarning( getResources().getString(R.string.chose_one ));
        }



        //finish();
    }



    public void displayWarning(  String message)
    {
        View view;
        TextView text;

        Toast toast = Toast.makeText(this, R.string.chose_one, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 0);
        view = toast.getView();
        view.setBackgroundColor(Color.rgb(255,0,0));

        text = view.findViewById(android.R.id.message);
        text.setTextColor(Color.rgb(255,255,255));
        text.setShadowLayer(20,0,0,0);
        toast.show();
        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
        r.play();
    }


    public void reactivateImport( View v)
    {
        boolean checked;
        Button btn = (Button) findViewById(R.id.btn_run);

        if (btn != null) {
            checked = ((CheckBox) findViewById(R.id.checkBoxUsuarios)).isChecked();

            if (!checked)
                checked = ((CheckBox) findViewById(R.id.checkBoxUbicaciones)).isChecked();

            if (!checked)
                checked = ((CheckBox) findViewById(R.id.checkBoxProductos)).isChecked();

            if (!checked)
                checked = ((CheckBox) findViewById(R.id.checkBoxDepartamentos)).isChecked();

            if (!checked)
                checked = ((CheckBox) findViewById(R.id.checkBoxSubFamilias)).isChecked();

            if (!checked)
                checked = ((CheckBox) findViewById(R.id.checkBoxTarifas)).isChecked();

            if (!checked)
                checked = ((CheckBox) findViewById(R.id.checkBoxComentarios)).isChecked();

            if (!checked)
                checked = ((CheckBox) findViewById(R.id.checkBoxIva)).isChecked();

            if (!checked)
                checked = ((CheckBox) findViewById(R.id.checkBoxTarifasSub)).isChecked();
            btn.setEnabled(checked);

        }
    }

    public void checkAll( View v)
    {
        CheckBox cb;
        boolean checkState;

        cb = findViewById(R.id.checkBoxAll);
        checkState = cb.isChecked();

        cb = findViewById(R.id.checkBoxUsuarios);
        cb.setChecked(checkState);

        cb = findViewById(R.id.checkBoxUbicaciones);
        cb.setChecked(checkState);

        cb = findViewById(R.id.checkBoxProductos);
        cb.setChecked(checkState);

        cb = findViewById(R.id.checkBoxDepartamentos);
        cb.setChecked(checkState);

        cb = findViewById(R.id.checkBoxSubFamilias);
        cb.setChecked(checkState);

        cb = findViewById(R.id.checkBoxTarifas);
        cb.setChecked(checkState);

        cb = findViewById(R.id.checkBoxTarifasSub);
        cb.setChecked(checkState);

        cb = findViewById(R.id.checkBoxComentarios);
        cb.setChecked(checkState);

        cb = findViewById(R.id.checkBoxIva);
        cb.setChecked(checkState);

        reactivateImport(v);

    }

    public void terminar( View v)
    {
        //Log.d("Importing","Exiting...");
        finish();
    }

    public class ImportarSituacion extends AsyncTask<Integer,Integer,String> {
        private String z = "Simply perfect";
        private Boolean isSuccess = false;
        private int rowCount=0;
        private Situacion situacion = new Situacion();
        private InputStream bitmapBlob;
        private Bitmap bmp;
        private Bitmap bmptmp;

        SQLiteTPVR sqlite= new SQLiteTPVR(m_context);;


        ProgressBar progressbar = findViewById(R.id.progressBarUbicaciones);
        TextView tv = findViewById(R.id.textViewPCUbicaciones);




        @Override
        protected void onPreExecute() {
            progressbar.setProgress(0);
            tv.setText("0%");
            sqlite.deleteAllrecords("Situacion");
            Log.d("Importer", "Import des ubicaciones");
        }

        @Override
        protected void onPostExecute(String r) {
            DecNbProcesses();

            if (isSuccess){
                Log.d("Importer", "OK");
            }
            else
            {
                Log.d("Erreur", z);
            }

        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            //Log.d("Progress", String.valueOf(values[0]));
            progressbar.setProgress(values[0]);

            tv.setText(String.format("%d%%",(100*(values[0])/rowCount)));
        }

        @Override
        protected String doInBackground(Integer... params) {

            int position = 0;
            String query;
            Statement stmt;
            ResultSet rs;



            try {
                // Connexion a la base de données TPVR avec les paramètres du serveur par défaut
                Connection con = ConnectionClass.CONN(DBTPVR, SyncDatabase.this);

                if (con == null) {
                    z = "Error in connection with SQL server";
                } else {

                    // Get the number of rows in the table
                    query = "SELECT COUNT(Clave) FROM ["+DBTPVR+"].[dbo].[Situacion]";
                    //Log.d("Query", query);

                    try {
                        stmt = con.createStatement();
                        rs = stmt.executeQuery(query);

                        rs.next();
                        rowCount = rs.getInt(1);

                        rs.close();

                        query = "SELECT Clave\n" +
                                "      ,Descripcion\n" +
                                "      ,Mesas\n" +
                                "      ,Tarifa\n" +
                                "      ,Img\n" +
                                "      ,PrimeraMesa\n" +
                                "      ,Impresora\n" +
                                "      ,Driver\n" +
                                "      ,Port\n" +
                                "  FROM ["+DBTPVR+"].[dbo].[Situacion]";


                        //Log.d("Query", query);
                        try {


                            try {
                                rs = stmt.executeQuery(query);

                                isSuccess = true;
                                progressbar.setMax(rowCount);

                                SQLiteDatabase db = sqlite.getWritableDatabase();
                                while (rs.next()) {

                                    //Log.d("Añadiendo sala", rs.getString("Descripcion"));


                                    // Extracting bitmap
                                    bitmapBlob = rs.getBinaryStream("Img");

                                    if(bitmapBlob != null) {
                                        BufferedInputStream bufferedInputStream = new BufferedInputStream(bitmapBlob);
                                        //Log.d("Bitmap", "Decoding blob");
                                        bmptmp = BitmapFactory.decodeStream(bufferedInputStream);
                                        bmp = bmptmp.copy(bmptmp.getConfig(), true);
                                    }
                                    else
                                        bmp = null;

                                    // Adressing all columns in the table to the situacion object
                                    situacion.setClave(rs.getString("Clave"));
                                    situacion.setDescription(rs.getString("Descripcion"));
                                    situacion.setMesas(rs.getInt("Mesas"));
                                    situacion.setTarifa(rs.getString("Tarifa"));
                                    situacion.setBitmap(bmp);
                                    situacion.setPrimeraMesa(rs.getString("PrimeraMesa"));
                                    situacion.setImpresora(rs.getString("Impresora"));
                                    situacion.setDriver(rs.getString("Driver"));
                                    situacion.setPort(rs.getString("Port"));

                                    ++position;
                                    publishProgress(position);

                                    sqlite.addSituacion(situacion, db);
                                    //Thread.sleep(500);
                                }
                                db.close();


                            } catch (Exception ex) {
                                z = "Exception añadiendo: " + ex.getMessage();
                                //Log.d("ER/Stmt", z);
                            }


                        } catch (Exception ex) {
                            z = "Exception select: " + ex.getMessage();
                            //Log.d("ER/Stmt", z);
                        }
                    }
                    catch(Exception ex){
                        z = "Exception count: " + ex.getMessage();
                        //Log.d("ER/Stmt", z);
                    }

                }
            } catch (Exception ex) {
                isSuccess = false;

                Log.e("ERROR:", ex.getMessage());
                z = getResources().getString(R.string.error_leer_datos ) +"  " + ex.getMessage();
                displayWarning( z);

            }


            return z;
        }


    }

    public class ImportarUsuarios extends AsyncTask<Integer,Integer,String> {
        private String z = "Simply perfect";
        private Boolean isSuccess = false;
        private int rowCount=0;
        private String query;
        private Statement stmt;
        private ResultSet rs;
        private Connection con;
        private Usuario usuario = new Usuario();
        ProgressBar progressbar = findViewById(R.id.progressBarUsuarios);
        TextView tv = findViewById(R.id.textViewPCUsuarios);
        SQLiteTPVR sqlite= new SQLiteTPVR(m_context);;






        @Override
        protected void onPreExecute() {
            progressbar.setProgress(0);
            tv.setText("0%");
            sqlite.deleteAllrecords("Usuarios");
            Log.d("Importer", "Usuarios");
        }

        @Override
        protected void onPostExecute(String r) {
            DecNbProcesses();
            if (isSuccess){
                Log.d("Importer", "OK");
            }
            else
            {
                Log.d("Erreur", z);
            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            //Log.d("Progress", String.valueOf(values[0]));
            progressbar.setProgress(values[0]);
            tv.setText(String.format("%d%%",(100*(values[0])/rowCount)));
        }

        @Override
        protected String doInBackground(Integer... params) {

            int position = 0;




            try {
                // Connexion a la base de données TPVR avec les paramètres du serveur par défaut
                con = ConnectionClass.CONN(DBTPVR, SyncDatabase.this);

                if (con == null) {
                    z = "Error in connection with SQL server";
                } else {

                    // Get the number of rows in the table
                    query = "SELECT COUNT(Numero) FROM ["+DBTPVR+"].[dbo].[Usuarios]";
                    //Log.d("Query", query);

                    try {
                        stmt = con.createStatement();
                        rs = stmt.executeQuery(query);
                        rs.next();
                        rowCount = rs.getInt(1);
                        rs.close();

                        //Log.d("Rowcount",String.format("%d",rowCount));

                        query = "SELECT Numero\n" +
                                "      ,Usuario\n" +
                                "  FROM ["+DBTPVR+"].[dbo].[Usuarios]";



                        //Log.d("Query", query);
                        try {


                            try {
                                rs = stmt.executeQuery(query);

                                isSuccess = true;
                                progressbar.setMax(rowCount);
                                SQLiteDatabase db = sqlite.getWritableDatabase();

                                while (rs.next()) {

                                    //Log.d("Añadiendo usuario", rs.getString("Usuario"));

                                    usuario.setId(rs.getInt("Numero"));
                                    usuario.setName(rs.getString("Usuario"));

                                    ++position;
                                    publishProgress(position);
                                    sqlite.addUsuario(usuario, db);
                                    //Thread.sleep(500);
                                }
                                db.close();


                            } catch (Exception ex) {
                                z = "Exception add: " + ex.getMessage();
                                Log.e("ER/Stmt", z);
                                z = getResources().getString(R.string.error_guardar_datos ) +"  " + ex.getMessage();
                                displayWarning( z);
                            }


                        } catch (Exception ex) {
                            z = "Exception select: " + ex.getMessage();
                            Log.e("ER/Stmt", z);
                            z = getResources().getString(R.string.error_leer_datos ) +"  " + ex.getMessage();
                            displayWarning( z);
                        }
                    }
                    catch(Exception ex){
                        z = "Exception count: " + ex.getMessage();
                        Log.e("ER/Stmt", z);
                        z = getResources().getString(R.string.error_leer_datos ) +"  " + ex.getMessage();
                        displayWarning( z);                   }

                }
            } catch (Exception ex) {
                isSuccess = false;
                z = "Exception: " + ex.getMessage();

                Log.e("ERROR:", ex.getMessage());
                z = getResources().getString(R.string.error_leer_datos ) +"  " + ex.getMessage();
                displayWarning( z);
            }


            return z;
        }


    }

    public class ImportarProductos extends AsyncTask<Integer,Integer,String> {
        private String z = "Simply perfect";
        private Boolean isSuccess = false;
        private int rowCount=0;
        private String query;
        private Statement stmt;
        private ResultSet rs;
        private Connection con;
        SQLiteTPVR sqlite= new SQLiteTPVR(m_context);
        private Articulos art = new Articulos();
        ProgressBar progressbar = findViewById(R.id.progressBarProductos);
        private TextView tv = findViewById(R.id.textViewPCProductos);
        private InputStream bitmapBlob;
        private Bitmap bmp;
        private Bitmap bmptmp;



        @Override
        protected void onPreExecute() {
            progressbar.setProgress(0);
            tv.setText("0%");
            sqlite.deleteAllrecords("Articulos");
            Log.d("Importer", "Produits");
        }

        @Override
        protected void onPostExecute(String r) {
            DecNbProcesses();
            if (isSuccess){
                Log.d("Importer", "OK");
            }
            else
            {
                Log.d("Erreur", z);
            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            //Log.d("Progress", String.valueOf(values[0]));
            progressbar.setProgress(values[0]);

            tv.setText(String.format("%d%%",(100*(values[0])/rowCount)));
        }

        @Override
        protected String doInBackground(Integer... params) {

            int position = 0;




            try {
                // Connexion a la base de données DI00001 avec les paramètres du serveur par défaut
                con = ConnectionClass.CONN(DB, SyncDatabase.this);

                if (con == null) {
                    z = "Error in connection with SQL server";
                } else {

                    // Get the number of rows in the table
                    query = "SELECT COUNT(Codigo) FROM ["+DB+"].[dbo].[Articulos]  WHERE Articulos.MostrarTPV = 1";
                    //Log.d("Query", query);

                    try {
                        stmt = con.createStatement();
                        rs = stmt.executeQuery(query);
                        rs.next();
                        rowCount = rs.getInt(1);
                        rs.close();

                        //Log.d("Rowcount",String.format("%d",rowCount));

                        query = "SELECT Articulos.Id\n" +
                                " ,Articulos.Codigo\n" +
                                " ,Articulos.Descripcion\n" +
                                " ,Articulos.Almacen\n" +
                                " ,Articulos.TipoIva\n" +
                                " ,Articulos.Departamento\n" +
                                " ,Articulos.Notas\n" +
                                " ,Articulos.Componentes\n" +
                                " ,Articulos.ImgArt\n" +
                                " ,Articulos.DescTPV\n" +
                                " ,Articulos.MostrarTPV\n" +
                                " ,Articulos.Orden\n" +
                                " ,Articulos.Comanda\n" +
                                " ,Articulos.Compuesto\n" +
                                " ,Articulos.Incremento\n" +
                                " ,Articulos.ConSubFam\n" +
                                " ,Articulos.NoEsSubFam\n" +
                                " ,Articulos.PedirPrecio0\n" +
                                " ,Articulos.Comentarios\n" +
                                " ,Articulos.SumaCantidad\n" +
                                " ,Articulos.SeleccionColor\n" +
                                " ,Articulos.Color\n" +
                                " , ISNULL(db.nbSubFamilias, 0) AS nbSubFamilias\n" +
                                " FROM ["+DB+"].[dbo].[Articulos]\n" +
                                "               LEFT JOIN \n" +
                                " (SELECT [CodArticulo]\n" +
                                "      ,count(*) AS nbSubFamilias\n" +
                                "  FROM ["+DB+"].[dbo].[ArtSubFamilias]\n" +
                                "  GROUP BY [CodArticulo]) db\n" +
                                "  ON Articulos.Codigo = db.CodArticulo"+
                                "  WHERE Articulos.MostrarTPV = 1";



                        Log.d("Query", query);
                        try {


                            try {
                                rs = stmt.executeQuery(query);

                                isSuccess = true;
                                progressbar.setMax(rowCount);
                                SQLiteDatabase db = sqlite.getWritableDatabase();

                                while (rs.next()) {

                                    Log.d("Añadiendo producto", rs.getString("Descripcion"));
                                    // Extracting bitmap
                                    bitmapBlob = rs.getBinaryStream("ImgArt");

                                    if(bitmapBlob != null) {
                                        BufferedInputStream bufferedInputStream = new BufferedInputStream(bitmapBlob);
                                        //Log.d("Bitmap", "Decoding blob");
                                        bmptmp = BitmapFactory.decodeStream(bufferedInputStream);
                                        bmp = bmptmp.copy(bmptmp.getConfig(), true);
                                    }
                                    else
                                        bmp = null;
                                    art.setId( rs.getInt("Id"));
                                    art.setCodigo( rs.getString("Codigo"));
                                    art.setDescription( rs.getString("Descripcion"));
                                    art.setAlmacen( rs.getString("Almacen"));
                                    art.setTipoIva( rs.getString("TipoIva"));
                                    art.setDepartamento( rs.getString("Departamento"));
                                    art.setNotas( rs.getString("Notas"));
                                    art.setComponentes( rs.getString("Componentes"));
                                    art.setBitmap( bmp);
                                    art.setDescTPV( rs.getString("DescTPV"));
                                    art.setMostrarTPV( rs.getInt("MostrarTPV"));
                                    art.setOrden( rs.getInt("Orden"));
                                    art.setComanda( rs.getInt("Comanda"));
                                    art.setCompuesto( rs.getInt("Compuesto"));
                                    art.setIncremento( rs.getDouble("Incremento"));
                                    art.setConSubFam( rs.getBoolean("ConSubFam") ? 1:0 );
                                    art.setNoEsSubFam( rs.getBoolean("NoEsSubFam") ? 1:0);
                                    art.setPedirPrecio( rs.getBoolean("PedirPrecio0") ? 1:0);
                                    art.setComentarios( rs.getBoolean("Comentarios") ? 1:0);
                                    art.setSumaCantidad( rs.getBoolean("SumaCantidad") ? 1:0);
                                    art.setSeleccionColor( rs.getBoolean("SeleccionColor") ? 1:0);
                                    art.setColor( rs.getString("Color"));
                                    art.setNbSubFamilias(rs.getInt("nbSubFamilias"));
                                    art.setBlob( rs.getBytes("ImgArt"));
                                    art.setComanda(rs.getInt("Comanda"));
                                    sqlite.addArticulo(art, db);

                                    ++position;
                                    publishProgress(position);
                                    //Thread.sleep(500);
                                }
                                db.close();


                            } catch (Exception ex) {
                                z = "Exception add: " + ex.getMessage();
                                Log.e("ER/Stmt", z);
                            }


                        } catch (Exception ex) {
                            z = "Exception select: " + ex.getMessage();
                            Log.e("ER/Stmt", z);
                        }
                    }
                    catch(Exception ex){
                        z = "Exception count: " + ex.getMessage();
                        Log.e("ER/Stmt", z);
                    }

                }
            } catch (Exception ex) {
                isSuccess = false;
                z = "Exception: " + ex.getMessage();

                //Log.e("ERROR:", ex.getMessage());
            }


            return z;
        }


    }

    public class ImportarSubfamilias extends AsyncTask<Integer,Integer,String> {
        private String z = "Simply perfect";
        private Boolean isSuccess = false;
        private int rowCount=0;
        private String query;
        private Statement stmt;
        private ResultSet rs;
        private Connection con;
        ProgressBar progressbar = findViewById(R.id.progressBarSubfamilias);
        TextView tv = findViewById(R.id.textViewPCSubfamilias);
        SubFamilias subfam = new SubFamilias();
        SQLiteTPVR sqlite= new SQLiteTPVR(m_context);

        //List<SubFamilias> sflst = new ArrayList<SubFamilias>();





        @Override
        protected void onPreExecute() {
            progressbar.setProgress(0);
            tv.setText("0%");
            sqlite.deleteAllrecords("ArtSubfamilias");
            Log.d("Importer", "Art Subfamilias");
        }

        @Override
        protected void onPostExecute(String r) {
            DecNbProcesses();
            if (isSuccess){
                Log.d("Importer", "OK");
            }
            else
            {
                Log.d("Erreur", z);
            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            //Log.d("Progress", String.valueOf(values[0]));
            progressbar.setProgress(values[0]);
            tv.setText(String.format("%d%%",(100*(values[0])/rowCount)));
        }

        @Override
        protected String doInBackground(Integer... params) {

            int position = 0;




            try {
                // Connexion a la base de données TPVR avec les paramètres du serveur par défaut
                con = ConnectionClass.CONN(DB, SyncDatabase.this);

                if (con == null) {
                    z = "Error in connection with SQL server";
                } else {

                    // Get the number of rows in the table
                    query = "SELECT COUNT(CodArticulo) FROM ["+DB+"].[dbo].[ArtSubFamilias]";
                    //Log.d("Query", query);

                    try {
                        stmt = con.createStatement();
                        rs = stmt.executeQuery(query);
                        rs.next();
                        rowCount = rs.getInt(1);
                        rs.close();

                        //Log.d("Rowcount",String.format("%d",rowCount));

                        query = "SELECT CodArticulo\n" +
                                "      ,Descripcion\n" +
                                "      ,Componente\n" +
                                "      ,DComponente\n" +
                                "      ,CantidadSubFamilia\n" +
                                "      ,CantidadPrincipal\n" +
                                "      ,PedirPrecio0\n" +
                                "  FROM ["+DB+"].[dbo].[ArtSubFamilias]";



                        //Log.d("Query", query);
                        try {


                            try {
                                rs = stmt.executeQuery(query);

                                isSuccess = true;
                                progressbar.setMax(rowCount);

                                SQLiteDatabase db = sqlite.getWritableDatabase();
                                while (rs.next()) {

                                    //Log.d("Añadiendo subfamilia", rs.getString("Descripcion"));
                                    subfam = new SubFamilias();
                                    subfam.setCodArticulo(rs.getString("CodArticulo"));
                                    subfam.setDescripcion(rs.getString("Descripcion"));
                                    subfam.setComponente(rs.getString("Componente"));
                                    subfam.setDComponente(rs.getString("DComponente"));
                                    subfam.setCantidadSubFamilia(rs.getDouble("CantidadSubFamilia"));
                                    subfam.setCantidadPrincipal(rs.getDouble("CantidadPrincipal"));
                                    subfam.setPedirPrecio0(rs.getInt("PedirPrecio0"));

                                    //sflst.add(subfam);
                                    sqlite.addSubfamilia(subfam,db);

                                    ++position;
                                    publishProgress(position);
                                    //Thread.sleep(500);
                                }
                                //sqlite.addSubfamilia(sflst);
                                db.close();


                            } catch (Exception ex) {
                                z = "Exception add: " + ex.getMessage();
                                //Log.d("ER/Stmt", z);
                            }


                        } catch (Exception ex) {
                            z = "Exception select: " + ex.getMessage();
                            Log.e("ER/Stmt", z);
                        }
                    }
                    catch(Exception ex){
                        z = "Exception count: " + ex.getMessage();
                        Log.e("ER/Stmt", z);
                    }

                }
            } catch (Exception ex) {
                isSuccess = false;
                z = "Exception: " + ex.getMessage();

                Log.e("ERROR:", ex.getMessage());
            }

            return z;
        }


    }

    public class ImportarTarifas extends AsyncTask<Integer,Integer,String> {
        private String z = "Simply perfect";
        private Boolean isSuccess = false;
        private int rowCount=0;
        private String query;
        private Statement stmt;
        private ResultSet rs;
        private Connection con;
        private Tarifas tar = new Tarifas();
        Date fecha;

        ProgressBar progressbar = findViewById(R.id.progressBarTarifas);
        TextView tv = findViewById(R.id.textViewPCTarifas);
        SQLiteTPVR sqlite= new SQLiteTPVR(m_context);



        @Override
        protected void onPreExecute() {

            progressbar.setProgress(0);
            tv.setText("0%");
            sqlite.deleteAllrecords("TarifasArticulo");
            Log.d("Importer", "Tarifs");
        }

        @Override
        protected void onPostExecute(String r) {
            DecNbProcesses();
            if (isSuccess){
                Log.d("Importer", "OK");
            }
            else
            {
                Log.d("Erreur", z);
            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            //Log.d("Progress", String.valueOf(values[0]));
            progressbar.setProgress(values[0]);

            tv.setText(String.format("%d%%",(100*(values[0])/rowCount)));
        }

        @Override
        protected String doInBackground(Integer... params) {

            int position = 0;




            try {
                // Connexion a la base de données TPVR avec les paramètres du serveur par défaut
                con = ConnectionClass.CONN(DB, SyncDatabase.this);

                if (con == null) {
                    z = "Error in connection with SQL server";
                } else {

                    // Get the number of rows in the table
                    query = "SELECT COUNT(Ntarifa) FROM ["+DB+"].[dbo].[TarifasArticulo]";
                    //Log.d("Query", query);

                    try {
                        stmt = con.createStatement();
                        rs = stmt.executeQuery(query);
                        rs.next();
                        rowCount = rs.getInt(1);
                        rs.close();

                        //Log.d("Rowcount",String.format("%d",rowCount));

                        query = "SELECT [Id]\n" +
                                "      ,[Ntarifa]\n" +
                                "      ,[Fecha]\n" +
                                "      ,[CodArtic]\n" +
                                "      ,[Precio]\n" +
                                "      ,[PrecioSubFamilia]\n" +
                                "  FROM ["+DB+"].[dbo].[TarifasArticulo]";



                        //Log.d("Query", query);
                        try {


                            try {
                                rs = stmt.executeQuery(query);

                                isSuccess = true;
                                progressbar.setMax(rowCount);

                                SQLiteDatabase db = sqlite.getWritableDatabase();
                                while (rs.next()) {

                                    //Log.d("Añadiendo tarifa", rs.getString("Ntarifa") +"/"+rs.getString("CodArtic"));
                                    tar.setId( rs.getInt("Id"));
                                    tar.setNtarifa( rs.getString("Ntarifa"));
                                    fecha = rs.getDate("Fecha");
                                    if(fecha != null)
                                        tar.setFecha( df.format(rs.getDate("Fecha")));
                                    tar.setCodArtic( rs.getString("CodArtic"));
                                    tar.setPrecio( rs.getDouble("Precio"));
                                    tar.setPrecioSubFamilia( rs.getDouble("PrecioSubFamilia"));

                                    sqlite.addTarifa(tar, db);
                                    ++position;
                                    publishProgress(position);
                                    //Thread.sleep(500);
                                }
                                db.close();

                            } catch (Exception ex) {
                                z = "Exception add: " + ex.getMessage();
                                Log.e("ER/Stmt", z);
                            }


                        } catch (Exception ex) {
                            z = "Exception select: " + ex.getMessage();
                            Log.e("ER/Stmt", z);
                        }
                    }
                    catch(Exception ex){
                        z = "Exception count: " + ex.getMessage();
                        Log.e("ER/Stmt", z);
                    }

                }
            } catch (Exception ex) {
                isSuccess = false;
                z = "Exception: " + ex.getMessage();

                Log.e("ERROR:", ex.getMessage());
            }


            return z;
        }


    }

    public class ImportarTarifasSubFamilia extends AsyncTask<Integer,Integer,String> {
        private String z = "Simply perfect";
        private Boolean isSuccess = false;
        private int rowCount=0;
        private String query;
        private Statement stmt;
        private ResultSet rs;
        private Connection con;
        private TarifasSubFamilias tar = new TarifasSubFamilias();
        Date fecha;

        ProgressBar progressbar = findViewById(R.id.progressBarTarifasSub);
        TextView tv = findViewById(R.id.textViewPCTarifasSub);
        SQLiteTPVR sqlite= new SQLiteTPVR(m_context);



        @Override
        protected void onPreExecute() {

            progressbar.setProgress(0);
            tv.setText("0%");
            sqlite.deleteAllrecords("TarifasArtSubFamilia");
            Log.d("Importer", "Tarifs sous familles");
        }

        @Override
        protected void onPostExecute(String r) {
            DecNbProcesses();
            if (isSuccess){
                Log.d("Importer", "OK");
            }
            else
            {
                Log.d("Erreur", z);
            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            //Log.d("Progress", String.valueOf(values[0]));
            progressbar.setProgress(values[0]);

            tv.setText(String.format(Locale.FRANCE,"%d%%",(100*(values[0])/rowCount)));
        }

        @Override
        protected String doInBackground(Integer... params) {

            int position = 0;




            try {
                // Connexion a la base de données TPVR avec les paramètres du serveur par défaut
                con = ConnectionClass.CONN(DB, SyncDatabase.this);

                if (con == null) {
                    z = "Error in connection with SQL server";
                } else {

                    // Get the number of rows in the table
                    query = "SELECT COUNT(CodArticulo) FROM ["+DB+"].[dbo].[TarifasArtSubFamilia]";
                    //Log.d("Query", query);

                    try {
                        stmt = con.createStatement();
                        rs = stmt.executeQuery(query);
                        rs.next();
                        rowCount = rs.getInt(1);
                        rs.close();

                        //Log.d("Rowcount",String.format("%d",rowCount));

                        query = "SELECT [CodArticulo]\n" +
                                "      ,[Descripcion]\n" +
                                "      ,[Componente]\n" +
                                "      ,[DComponente]\n" +
                                "      ,[NTarifa]\n" +
                                "      ,[dTarifa]\n" +
                                "      ,[Precio]\n" +
                                "  FROM ["+DB+"].[dbo].[TarifasArtSubFamilia]";



                        //Log.d("Query", query);
                        try {


                            try {
                                rs = stmt.executeQuery(query);

                                isSuccess = true;
                                progressbar.setMax(rowCount);

                                SQLiteDatabase db = sqlite.getWritableDatabase();
                                while (rs.next()) {

                                    //Log.d("Añadiendo tarifa subfamilia", rs.getString("Ntarifa") +"/"+rs.getString("CodArtic"));
                                    tar.setCodArticulo(rs.getString("CodArticulo"));
                                    tar.setDescripcion(rs.getString("Descripcion"));
                                    tar.setComponente(rs.getString("Componente"));
                                    tar.setDComponente(rs.getString("DComponente"));
                                    tar.setNTarifa(rs.getString("NTarifa"));
                                    tar.setdTarifa(rs.getString("dTarifa"));
                                    tar.setPrecio(rs.getDouble("Precio"));

                                    sqlite.addTarifaSubFamilia(tar, db);
                                    ++position;
                                    publishProgress(position);
                                    //Thread.sleep(500);
                                }
                                db.close();

                            } catch (Exception ex) {
                                z = "Exception add: " + ex.getMessage();
                                Log.e("ER/Stmt", z);
                            }


                        } catch (Exception ex) {
                            z = "Exception select: " + ex.getMessage();
                            Log.e("ER/Stmt", z);
                        }
                    }
                    catch(Exception ex){
                        z = "Exception count: " + ex.getMessage();
                        Log.e("ER/Stmt", z);
                    }

                }
            } catch (Exception ex) {
                isSuccess = false;
                z = "Exception: " + ex.getMessage();

                Log.e("ERROR:", ex.getMessage());
            }


            return z;
        }


    }

    public class ImportarDepartamentos extends AsyncTask<Integer,Integer,String> {
        private String z = "Simply perfect";
        private Boolean isSuccess = false;
        private int rowCount=0;
        private String query;
        private Statement stmt;
        private ResultSet rs;
        private Connection con;
        private Departamentos dpt = new Departamentos();
        private InputStream bitmapBlob;
        private Bitmap bmp;
        private Bitmap bmptmp;
        SQLiteTPVR sqlite= new SQLiteTPVR(m_context);
        ProgressBar progressbar = findViewById(R.id.progressBarDepartamentos);
        TextView tv = findViewById(R.id.textViewPCDepartamentos);



        @Override
        protected void onPreExecute() {

            progressbar.setProgress(0);
            tv.setText("0%");
            sqlite.deleteAllrecords("Departamentos");
            Log.d("Importer", "Departamentos");
        }

        @Override
        protected void onPostExecute(String r) {
            DecNbProcesses();
            if (isSuccess){
                Log.d("Importer", "OK");
            }
            else
            {
                Log.d("Erreur", z);
            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            //Log.d("Progress", String.valueOf(values[0]));
            progressbar.setProgress(values[0]);

            tv.setText(String.format( "%d%%",(100*(values[0])/rowCount)));
        }

        @Override
        protected String doInBackground(Integer... params) {

            int position = 0;




            try {
                // Connexion a la base de données TPVR avec les paramètres du serveur par défaut
                con = ConnectionClass.CONN(DB, SyncDatabase.this);

                if (con == null) {
                    z = "Error in connection with SQL server";
                } else {

                    // Get the number of rows in the table
                    query = "SELECT COUNT(Codigo) FROM ["+DB+"].[dbo].[Departamentos]  WHERE [MostrarTPV] <> 0";
                    //Log.d("Query", query);

                    try {
                        stmt = con.createStatement();
                        rs = stmt.executeQuery(query);
                        rs.next();
                        rowCount = rs.getInt(1);
                        rs.close();

                        //Log.d("Rowcount",String.format("%d",rowCount));

                        query = "SELECT [Codigo]\n" +
                                "      ,[Descripcion]\n" +
                                "      ,[Cuenta]\n" +
                                "      ,[Img]\n" +
                                "      ,[MostrarTPV]\n" +
                                "      ,[Orden]\n" +
                                "  FROM ["+DB+"].[dbo].[Departamentos]"+
                                "  WHERE [MostrarTPV] <> 0";



                        //Log.d("Query", query);
                        try {


                            try {
                                rs = stmt.executeQuery(query);

                                isSuccess = true;
                                progressbar.setMax(rowCount);

                                SQLiteDatabase db = sqlite.getWritableDatabase();
                                while (rs.next()) {

                                    //Log.d("Añadiendo departamento", rs.getString("Descripcion"));
                                    // Extracting bitmap
                                    bitmapBlob = rs.getBinaryStream("Img");

                                    if(bitmapBlob != null) {
                                        BufferedInputStream bufferedInputStream = new BufferedInputStream(bitmapBlob);
                                        //Log.d("Bitmap", "Decoding blob");
                                        bmptmp = BitmapFactory.decodeStream(bufferedInputStream);
                                        bmp = bmptmp.copy(bmptmp.getConfig(), true);
                                    }
                                    else
                                        bmp = null;

                                    dpt.setCodigo(rs.getString("Codigo"));
                                    dpt.setDescripcion(rs.getString("Descripcion"));
                                    dpt.setCuenta(rs.getString("Cuenta"));
                                    dpt.setBitmap(bmp);
                                    dpt.setMostrarTPV(rs.getInt("MostrarTPV"));
                                    dpt.setOrden(rs.getInt("Orden"));

                                    sqlite.addDepartamento(dpt, db);
                                    ++position;
                                    publishProgress(position);
                                    //Thread.sleep(500);
                                }
                                db.close();


                            } catch (Exception ex) {
                                z = "Exception add: " + ex.getMessage();
                                Log.e("ER/Stmt", z);

                            }


                        } catch (Exception ex) {
                            z = "Exception select: " + ex.getMessage();
                            Log.e("ER/Stmt", z);
                        }
                    }
                    catch(Exception ex){
                        z = "Exception count: " + ex.getMessage();
                        Log.e("ER/Stmt", z);
                    }

                }
            } catch (Exception ex) {
                isSuccess = false;
                z = "Exception: " + ex.getMessage();

                Log.e("ERROR:", ex.getMessage());
            }


            return z;
        }


    }

    public class ImportarComentarios extends AsyncTask<Integer,Integer,String> {
        private String z = "Simply perfect";
        private Boolean isSuccess = false;
        private int rowCount=0;
        private String query;
        private Statement stmt;
        private ResultSet rs;
        private Connection con;
        Comentarios com = new Comentarios();
        ProgressBar progressbar = findViewById(R.id.progressBarComentarios);
        TextView tv = findViewById(R.id.textViewPCComentarios);
        SQLiteTPVR sqlite= new SQLiteTPVR(m_context);



        @Override
        protected void onPreExecute() {
            progressbar.setProgress(0);
            tv.setText("0%");
            sqlite.deleteAllrecords("Comentarios");
            Log.d("Importer", "Usuarios");
        }

        @Override
        protected void onPostExecute(String r) {
            DecNbProcesses();
            if (isSuccess){
                Log.d("Importer", "OK");
            }
            else
            {
                Log.d("Erreur", z);
            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            //Log.d("Progress", String.valueOf(values[0]));
            progressbar.setProgress(values[0]);
            tv.setText(String.format("%d%%",(100*(values[0])/rowCount)));
        }

        @Override
        protected String doInBackground(Integer... params) {

            int position = 0;




            try {
                // Connexion a la base de données TPVR avec les paramètres du serveur par défaut
                con = ConnectionClass.CONN(DB, SyncDatabase.this);

                if (con == null) {
                    z = "Error in connection with SQL server";
                } else {

                    // Get the number of rows in the table
                    query = "SELECT COUNT(CodArticulo) FROM ["+DB+"].[dbo].[ArtComentarios]";
                    //Log.d("Query", query);

                    try {
                        stmt = con.createStatement();
                        rs = stmt.executeQuery(query);
                        rs.next();
                        rowCount = rs.getInt(1);
                        rs.close();

                        //Log.d("Rowcount",String.format("%d",rowCount));

                        query = "SELECT [CodArticulo]\n" +
                                "      ,[Descripcion]\n" +
                                "      ,[CodComentario]\n" +
                                "      ,[DComentario]\n" +
                                "  FROM ["+DB+"].[dbo].[ArtComentarios]";



                        //Log.d("Query", query);
                        try {


                            try {
                                rs = stmt.executeQuery(query);

                                isSuccess = true;
                                progressbar.setMax(rowCount);
                                SQLiteDatabase db = sqlite.getWritableDatabase();

                                while (rs.next()) {

                                    //Log.d("Añadiendo comentario", rs.getString("Descripcion") +"/"+rs.getString("DComentario"));
                                    com.setCodArticulo(rs.getString("CodArticulo"));
                                    com.setDescripcion(rs.getString("Descripcion"));
                                    com.setCodComentario(rs.getInt("CodComentario"));
                                    com.setDComentario(rs.getString("DComentario"));

                                    sqlite.addComentario(com, db);

                                    ++position;
                                    publishProgress(position);
                                    //Thread.sleep(500);
                                }
                                db.close();


                            } catch (Exception ex) {
                                z = "Exception add: " + ex.getMessage();
                                Log.e("ER/Stmt", z);
                            }


                        } catch (Exception ex) {
                            z = "Exception select: " + ex.getMessage();
                            Log.e("ER/Stmt", z);
                        }
                    }
                    catch(Exception ex){
                        z = "Exception count: " + ex.getMessage();
                        Log.e("ER/Stmt", z);
                    }

                }
            } catch (Exception ex) {
                isSuccess = false;
                z = "Exception: " + ex.getMessage();

                Log.e("ERROR:", ex.getMessage());
            }


            return z;
        }


    }

    public class ImportarIva extends AsyncTask<Integer,Integer,String> {
        private String z = "Simply perfect";
        private Boolean isSuccess = false;
        private int rowCount=0;
        private String query;
        private Statement stmt;
        private ResultSet rs;
        private Connection con;
        TiposIva tIva = new TiposIva();
        ProgressBar progressbar = findViewById(R.id.progressBarIva);
        TextView tv = findViewById(R.id.textViewPCIva);
        SQLiteTPVR sqlite= new SQLiteTPVR(m_context);


        @Override
        protected void onPreExecute() {

            progressbar.setProgress(0);
            tv.setText("0%");
            sqlite.deleteAllrecords("TiposIva");
            Log.d("Importer", "Types de TVA");
        }

        @Override
        protected void onPostExecute(String r) {
            DecNbProcesses();
            if (isSuccess){
                Log.d("Importer", "OK");
            }
            else
            {
                Log.d("Erreur", z);
            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            //Log.d("Progress", String.valueOf(values[0]));
            progressbar.setProgress(values[0]);
            tv.setText(String.format("%d%%",(100*(values[0])/rowCount)));
        }

        @Override
        protected String doInBackground(Integer... params) {

            int position = 0;

            try {
                // Connexion a la base de données TPVR avec les paramètres du serveur par défaut
                con = ConnectionClass.CONN(DB, SyncDatabase.this);

                if (con == null) {
                    z = "Error in connection with SQL server";
                } else {

                    // Get the number of rows in the table
                    query = "SELECT COUNT(Codigo) FROM ["+DB+"].[dbo].[TiposIVA]";
                    //Log.d("Query", query);

                    try {
                        stmt = con.createStatement();
                        rs = stmt.executeQuery(query);
                        rs.next();
                        rowCount = rs.getInt(1);
                        rs.close();

                        //Log.d("Rowcount",String.format("%d",rowCount));

                        query = "SELECT [Codigo]\n" +
                                "      ,[Descripcion]\n" +
                                "      ,[TpcIVA]\n" +
                                "      ,[TpcReq]\n" +
                                "      ,[CtaIVASop]\n" +
                                "      ,[CtaReqSop]\n" +
                                "      ,[CtaIVARep]\n" +
                                "      ,[CtaReqRep]\n" +
                                "  FROM ["+DB+"].[dbo].[TiposIVA]";



                        //Log.d("Query", query);
                        try {


                            try {
                                rs = stmt.executeQuery(query);

                                isSuccess = true;
                                progressbar.setMax(rowCount);
                                SQLiteDatabase db = sqlite.getWritableDatabase();

                                while (rs.next()) {

                                    //Log.d("Añadiendo Tipo iva", rs.getString("Descripcion"));
                                    tIva.setCodigo(rs.getString("Codigo"));
                                    tIva.setDescripcion(rs.getString("Descripcion"));
                                    tIva.setTpcIva(rs.getString("TpcIVA"));
                                    tIva.setTpcReq(rs.getString("TpcReq"));
                                    tIva.setctaIvaSop(rs.getString("CtaIVASop"));
                                    tIva.setCtaReqSop(rs.getString("CtaReqSop"));
                                    tIva.setCtaIVARep(rs.getString("CtaIVARep"));
                                    tIva.setCtaReqRep(rs.getString("CtaReqRep"));
                                    sqlite.addTipoIva(tIva, db);

                                    ++position;
                                    publishProgress(position);
                                    //Thread.sleep(500);
                                }
                                db.close();


                            } catch (Exception ex) {
                                z = "Exception add: " + ex.getMessage();
                                Log.e("ER/Stmt", z);
                            }


                        } catch (Exception ex) {
                            z = "Exception select: " + ex.getMessage();
                            Log.e("ER/Stmt", z);
                        }
                    }
                    catch(Exception ex){
                        z = "Exception count: " + ex.getMessage();
                        Log.e("ER/Stmt", z);
                    }

                }
            } catch (Exception ex) {
                isSuccess = false;
                z = "Exception: " + ex.getMessage();

                Log.e("ERROR:", ex.getMessage());
            }


            return z;
        }


    }

    public class ImportarArqueo extends AsyncTask<Integer,Integer,String> {
        private String z = "Simply perfect";
        private Boolean isSuccess = false;
        private int rowCount=0;
        private String query;
        private Statement stmt;
        private ResultSet rs;
        private Connection con;
        Arqueo arqueo = new Arqueo();
        ProgressBar progressbar = findViewById(R.id.progressBarLocal);
        TextView tv = findViewById(R.id.textViewPCLocal);
        SQLiteTPVR sqlite= new SQLiteTPVR(m_context);
        Date date;



        @Override
        protected void onPreExecute() {

            progressbar.setProgress(0);
            tv.setText("0%");
            sqlite.deleteAllrecords("Arqueo");
            Log.d("Importer", "Arqueo");
        }

        @Override
        protected void onPostExecute(String r) {
            DecNbProcesses();
            if (isSuccess){
                Log.d("Importer", "OK");
            }
            else
            {
                Log.d("Erreur", z);
            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            //Log.d("Progress", String.valueOf(values[0]));
            progressbar.setProgress(values[0]);
            tv.setText(String.format("%d%%",(100*(values[0])/rowCount)));
        }

        @Override
        protected String doInBackground(Integer... params) {

            int position = 0;

            try {
                // Connexion a la base de données TPVR avec les paramètres du serveur par défaut
                con = ConnectionClass.CONN("TPVR", SyncDatabase.this);

                if (con == null) {
                    z = "Error in connection with SQL server";
                } else {

                    // Get the number of rows in the table
                    query = "SELECT COUNT(Id) FROM [TPVR].[dbo].[Arqueo]";
                    //Log.d("Query", query);

                    try {
                        stmt = con.createStatement();
                        rs = stmt.executeQuery(query);
                        rs.next();
                        rowCount = rs.getInt(1);
                        rs.close();

                        //Log.d("Rowcount",String.format("%d",rowCount));

                        query = "SELECT [Id]\n" +
                                "      ,[FechaTurno]\n" +
                                "      ,[Turno]\n" +
                                "      ,[Caja]\n" +
                                "      ,[Almacen]\n" +
                                "      ,[Apertura]\n" +
                                "      ,[FechaApertura]\n" +
                                "      ,[EmpApertura]\n" +
                                "      ,[ImpArqApertura]\n" +
                                "      ,[Cerrado]\n" +
                                "      ,[FechaCierre]\n" +
                                "      ,[EmpCierre]\n" +
                                "      ,[ImpTickets]\n" +
                                "      ,[ImpArqCierre]\n" +
                                "      ,[ImpTotalCaja]\n" +
                                "      ,[ImpDescuadre]\n" +
                                "      ,[CierreZ]\n" +
                                "      ,[AB500]\n" +
                                "      ,[AB200]\n" +
                                "      ,[AB100]\n" +
                                "      ,[AB50]\n" +
                                "      ,[AB20]\n" +
                                "      ,[AB10]\n" +
                                "      ,[AB5]\n" +
                                "      ,[AM200]\n" +
                                "      ,[AM100]\n" +
                                "      ,[AM50]\n" +
                                "      ,[AM20]\n" +
                                "      ,[AM10]\n" +
                                "      ,[AM5]\n" +
                                "      ,[AM2]\n" +
                                "      ,[AM1]\n" +
                                "      ,[DB500]\n" +
                                "      ,[DB200]\n" +
                                "      ,[DB100]\n" +
                                "      ,[DB50]\n" +
                                "      ,[DB20]\n" +
                                "      ,[DB10]\n" +
                                "      ,[DB5]\n" +
                                "      ,[DM200]\n" +
                                "      ,[DM100]\n" +
                                "      ,[DM50]\n" +
                                "      ,[DM20]\n" +
                                "      ,[DM10]\n" +
                                "      ,[DM5]\n" +
                                "      ,[DM2]\n" +
                                "      ,[DM1]\n" +
                                "      ,[ImpPagos]\n" +
                                "      ,[ImpCobros]\n" +
                                "      ,[ImpTicketsT]\n" +
                                "      ,[ImpTicketsCh]\n" +
                                "  FROM ["+DBTPVR+"].[dbo].[Arqueo]" /*+
                                "WHERE Caja = '"+m_caja+"' AND Almacen = '"+m_almacen+"' AND Cerrado = 0"*/;
                                // On n'importe que ce qui n'est pas cloturé pour une la caisse et le dépot du terminal


                        //Log.d("Query", query);
                        try {


                            try {
                                rs = stmt.executeQuery(query);

                                isSuccess = true;
                                progressbar.setMax(rowCount);
                                SQLiteDatabase db = sqlite.getWritableDatabase();
                                String stFecha ="";

                                // On formate la date au format que SQLite comprend pour pouvoir traiter la chaine comme une date plus tard

                                while (rs.next()) {

                                    date = rs.getDate("FechaTurno");
                                    // transformer la date au format chaine vu que sqlite ne comprend pas les dates
                                    stFecha = df.format(date);
                                    Log.d("Fecha turno formateada", stFecha);

                                    //Log.d("Añadiendo arqueo", rs.getString("caja"));
                                    arqueo.setFechaTurno(stFecha);
                                    arqueo.setId(rs.getInt("Id"));
                                    arqueo.setTurno(rs.getInt("Turno"));
                                    arqueo.setCaja(rs.getString("Caja"));
                                    arqueo.setAlmacen(rs.getString("Almacen"));
                                    arqueo.setApertura(rs.getInt("Apertura"));
                                    date = rs.getDate("FechaApertura");
                                    if(date != null)
                                        stFecha = df.format(date);
                                    else
                                        stFecha = "";
                                    Log.d("Fecha apertura", stFecha);
                                    arqueo.setFechaApertura(stFecha);
                                    arqueo.setEmpApertura(rs.getString("EmpApertura"));
                                    arqueo.setImpArqApertura(rs.getDouble("ImpArqApertura"));
                                    arqueo.setCerrado(rs.getInt("Cerrado"));
                                    date = rs.getDate("FechaCierre");
                                    if(date != null)
                                           stFecha = df.format(date);
                                    else
                                           stFecha = "";
                                    Log.d("Fecha cierre formateada", stFecha);
                                    arqueo.setFechaCierre(stFecha);
                                    arqueo.setEmpCierre(rs.getString("EmpCierre"));
                                    arqueo.setImpTickets(rs.getDouble("ImpTickets"));
                                    arqueo.setImpArqCierre(rs.getDouble("ImpArqCierre"));
                                    arqueo.setImpTotalCaja(rs.getDouble("ImpTotalCaja"));
                                    arqueo.setImpDescuadre(rs.getDouble("ImpDescuadre"));
                                    arqueo.setCierreZ(rs.getInt("CierreZ"));
                                    arqueo.setAB500(rs.getInt("AB500"));
                                    arqueo.setAB200(rs.getInt("AB200"));
                                    arqueo.setAB100(rs.getInt("AB100"));
                                    arqueo.setAB50(rs.getInt("AB50"));
                                    arqueo.setAB20(rs.getInt("AB20"));
                                    arqueo.setAB10(rs.getInt("AB10"));
                                    arqueo.setAB5(rs.getInt("AB5"));
                                    arqueo.setAM200(rs.getInt("AM200"));
                                    arqueo.setAM100(rs.getInt("AM100"));
                                    arqueo.setAM50(rs.getInt("AM50"));
                                    arqueo.setAM20(rs.getInt("AM20"));
                                    arqueo.setAM10(rs.getInt("AM10"));
                                    arqueo.setAM5(rs.getInt("AM5"));
                                    arqueo.setAM2(rs.getInt("AM2"));
                                    arqueo.setAM1(rs.getInt("AM1"));
                                    arqueo.setDB500(rs.getInt("DB500"));
                                    arqueo.setDB200(rs.getInt("DB200"));
                                    arqueo.setDB100(rs.getInt("DB100"));
                                    arqueo.setDB50(rs.getInt("DB50"));
                                    arqueo.setDB20(rs.getInt("DB20"));
                                    arqueo.setDB10(rs.getInt("DB10"));
                                    arqueo.setDB5(rs.getInt("DB5"));
                                    arqueo.setDM200(rs.getInt("DM200"));
                                    arqueo.setDM100(rs.getInt("DM100"));
                                    arqueo.setDM50(rs.getInt("DM50"));
                                    arqueo.setDM20(rs.getInt("DM20"));
                                    arqueo.setDM10(rs.getInt("DM10"));
                                    arqueo.setDM5(rs.getInt("DM5"));
                                    arqueo.setDM2(rs.getInt("DM2"));
                                    arqueo.setDM1(rs.getInt("DM1"));
                                    arqueo.setImpPagos(rs.getDouble("ImpPagos"));
                                    arqueo.setImpCobros(rs.getDouble("ImpCobros"));
                                    arqueo.setImpTicketsT(rs.getDouble("ImpTicketsT"));
                                    arqueo.setImpTicketsCh(rs.getDouble("ImpTicketsCh"));

                                    sqlite.addArqueo(arqueo, db);

                                    ++position;
                                    publishProgress(position);
                                    //Thread.sleep(500);
                                }
                                db.close();


                            } catch (Exception ex) {
                                z = "Exception add: " + ex.getMessage();
                                Log.e("ER/Stmt", z);
                            }


                        } catch (Exception ex) {
                            z = "Exception select: " + ex.getMessage();
                            Log.e("ER/Stmt", z);
                        }
                    }
                    catch(Exception ex){
                        z = "Exception count: " + ex.getMessage();
                        Log.e("ER/Stmt", z);
                    }

                }
            } catch (Exception ex) {
                isSuccess = false;
                z = "Exception: " + ex.getMessage();

                Log.e("ERROR:", ex.getMessage());
            }


            return z;
        }


    }

    public class ImportarConfTicket extends AsyncTask<Integer,Integer,String> {
        private String z = "Simply perfect";
        private Boolean isSuccess = false;
        private int rowCount=0;
        private String query;
        private Statement stmt;
        private ResultSet rs;
        private Connection con;
        ConfTicket cnfTicket = new ConfTicket();
        ProgressBar progressbar = findViewById(R.id.progressBarLocal);
        TextView tv = findViewById(R.id.textViewPCLocal);
        SQLiteTPVR sqlite= new SQLiteTPVR(m_context);


        @Override
        protected void onPreExecute() {

            progressbar.setProgress(0);
            tv.setText("0%");
            sqlite.deleteAllrecords("ConfTicket");
            Log.d("Importer", "Conf ticket");
        }

        @Override
        protected void onPostExecute(String r) {
            DecNbProcesses();
            if (isSuccess){
                Log.d("Importer", "OK");
            }
            else
            {
                Log.d("Erreur", z);
            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            //Log.d("Progress", String.valueOf(values[0]));
            progressbar.setProgress(values[0]);
            tv.setText(String.format(Locale.FRANCE,"%d%%",(100*(values[0])/rowCount)));
        }

        @Override
        protected String doInBackground(Integer... params) {

            int position = 0;

            try {
                // Connexion a la base de données TPVR avec les paramètres du serveur par défaut
                con = ConnectionClass.CONN(DBTPVR, SyncDatabase.this);

                if (con == null) {
                    z = "Error in connection with SQL server";
                } else {

                    // Get the number of rows in the table
                    query = "SELECT COUNT(NTicket) FROM [TPVR].[dbo].[ConfTicket]";
                    //Log.d("Query", query);

                    try {
                        stmt = con.createStatement();
                        rs = stmt.executeQuery(query);
                        rs.next();
                        rowCount = rs.getInt(1);
                        rs.close();

                        //Log.d("Rowcount",String.format("%d",rowCount));

                        query = "SELECT TOP (1000) [NTicket]\n" +
                                "      ,[CodAlmacen]\n" +
                                "      ,[CodCaja]\n" +
                                "  FROM ["+DBTPVR+"].[dbo].[ConfTicket]";



                        //Log.d("Query", query);
                        try {


                            try {
                                rs = stmt.executeQuery(query);

                                isSuccess = true;
                                progressbar.setMax(rowCount);
                                SQLiteDatabase db = sqlite.getWritableDatabase();

                                while (rs.next()) {

                                    //Log.d("Añadiendo Tipo iva", rs.getString("Descripcion"));


                                    cnfTicket.setNTicket(rs.getString("NTicket"));
                                    cnfTicket.setCodAlmacen(rs.getString("CodAlmacen"));
                                    cnfTicket.setCodCaja( rs.getString("CodCaja"));


                                    sqlite.addconfTicket(cnfTicket, db);

                                    ++position;
                                    publishProgress(position);
                                    //Thread.sleep(500);
                                }
                                db.close();


                            } catch (Exception ex) {
                                z = "Exception add: " + ex.getMessage();
                                Log.e("ER/Stmt", z);
                            }


                        } catch (Exception ex) {
                            z = "Exception select: " + ex.getMessage();
                            Log.e("ER/Stmt", z);
                        }
                    }
                    catch(Exception ex){
                        z = "Exception count: " + ex.getMessage();
                        Log.e("ER/Stmt", z);
                    }

                }
            } catch (Exception ex) {
                isSuccess = false;
                z = "Exception: " + ex.getMessage();

                Log.e("ERROR:", ex.getMessage());
            }


            return z;
        }


    }
}

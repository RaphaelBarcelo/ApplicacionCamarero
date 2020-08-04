package com.example.applicacioncamarero.activity;


import android.support.v7.app.ActionBar;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import android.view.MenuItem;

import com.example.applicacioncamarero.BuildConfig;
import com.example.applicacioncamarero.Custom.CustomViewPager;

import com.example.applicacioncamarero.fragment.DepartamentosFragment;
import com.example.applicacioncamarero.fragment.TicketFragment;
import com.example.applicacioncamarero.R;


import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static android.app.AlertDialog.THEME_HOLO_DARK;


public class MesaActivity extends AppCompatActivity{

    Toolbar toolbar;
    TabLayout tabLayout;
    CustomViewPager viewPager;
    private String m_sala;
    private String m_mesa;
    private String m_tarifa;
    private String m_codUbicacion;
    private String m_ticket ="";
    private String m_camarero = "GANDALF";
    private TicketFragment m_ticketFragment = null;
    private DepartamentosFragment m_departamentosFragment = null;
    private String m_state = "";
    private ActionBar m_actionBar;






    public void updateTitle()
    {
        double total;
        if(m_ticketFragment != null)
            total = m_ticketFragment.getTotal();
        else
            total = 0;
        updateTitle(total);


    }

    public void updateTitle(double total)
    {

        String header = getString(R.string.table) +" " + m_mesa + String.format(Locale.FRANCE,getString(R.string.total_table),total);
        m_actionBar.setTitle(header);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // app icon in action bar clicked; goto parent activity.
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        Bundle b = this.getIntent().getExtras();
        m_camarero = b.getString("camarero", "GANDALF");
        m_codUbicacion = b.getString("codSala", "TE2");

        Log.d("Mesa activity cod sala", m_codUbicacion);

        m_sala = b.getString("sala", "TERRAZA 2");
        Log.d("Mesa activity sala", m_sala);

        m_mesa = b.getString("mesa", "3");

        m_tarifa = b.getString("tarifa", "01");

        m_state = b.getString("state", "");
        m_ticket = b.getString("ticket", "");

        setContentView(R.layout.activity_mesa);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        m_actionBar = getSupportActionBar();
        if (m_actionBar != null) {
           /* ******************************************************************
           * Supprimé jusqu'a ce qu'on voie pourquoi le back button ne fonctionne pas
           *
           *

            m_actionBar.setDisplayHomeAsUpEnabled(true);
            m_actionBar.setDisplayShowHomeEnabled(true);
            m_actionBar.setHomeButtonEnabled(true);
            ****************************************************** */
            // On va utiliser le title de la action bar pour afficher les infos
            m_actionBar.setDisplayShowTitleEnabled(true);

        }



        viewPager = (CustomViewPager) findViewById(R.id.viewpager);

        setupViewPager(viewPager);

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        /*findViewById(R.id.up_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });*/

        //TextView textViewTitle = (TextView)findViewById(R.id.textViewMesaTitle);
        //textViewTitle.setText("Mesa " + m_mesa +" - "+m_camarero);
        updateTitle();


    }
    @Override
    public void onBackPressed()
    {
        // Set ticket modified to true if you don´t want to leave
        boolean ticketModified = false;

        // If botomsheet is expanded hide the bottonsheet and set ticketModified = true not to leave.  ;
        if(m_departamentosFragment != null && m_departamentosFragment.istBottomSheetExpanded())
        {
            m_departamentosFragment.hideBottomSheet();
            ticketModified = true;
        }
        else // Else check if the ticket has been modified
        {
            // If ticket fragment is active
            if (m_ticketFragment != null && m_ticketFragment.isVisible()) {
                // If ticket has been modified
                ticketModified = m_ticketFragment.getticketModified();
                if (ticketModified) {
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                    alertDialogBuilder.setMessage(R.string.ticket_modified_text);
                    alertDialogBuilder.setTitle(R.string.ticket_modified_title);
                    alertDialogBuilder.setIcon(R.drawable.alert_512);

                    alertDialogBuilder.setPositiveButton(R.string.yes,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface arg0, int arg1) {

                                    finish();
                                }
                            });

                    alertDialogBuilder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //
                            // If user does not want to leave then do nothing
                        }
                    });

                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();
                }
                Log.d("Tcket is", ticketModified ? "Modified" : "Not modified");

            }
        }
        if(!ticketModified)
            super.onBackPressed();

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

    private void setupViewPager(CustomViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        m_ticketFragment = new TicketFragment();
        m_ticketFragment.setViewpager(viewPager);
        m_ticketFragment.setMesaActivity(this);



        m_departamentosFragment = new DepartamentosFragment();
        m_departamentosFragment.setTicketFragment(m_ticketFragment);
        m_departamentosFragment.setViewpager(viewPager);
        Bundle args = new Bundle();
        args.putString("codSala", m_codUbicacion);
        args.putString("sala", m_sala);
        args.putString("mesa", m_mesa);
        args.putString("tarifa", m_tarifa);
        args.putString("camarero", m_camarero);
        args.putString("ticket", m_ticket);
        m_ticketFragment.setArguments(args);
        m_departamentosFragment.setArguments(args);

        // Si le ticket n'a pas été émit
       // Option supprimée
        //if( !m_state.equalsIgnoreCase("E") )
        //    if (!BuildConfig.BUILD_VERSION.equals("DISENY")
              adapter.addFragment(m_departamentosFragment, getString(R.string.Productos));
        adapter.addFragment(m_ticketFragment, getString(R.string.ticket));

        //layout_departamento.addFragment(new TicketFragment(), "THREE");
        viewPager.setAdapter(adapter);

        int count = viewPager.getChildCount();

       /* View view = viewPager.getChildAt(0);
        view.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        view = viewPager.getChildAt(0);
        view.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);*/
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

}
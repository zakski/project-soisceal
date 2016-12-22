package andrea.tuprolog.test.mytestapplication;

import android.os.AsyncTask;
import android.os.Bundle;

import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


import alice.tuprolog.InvalidTheoryException;
import alice.tuprolog.NoMoreSolutionException;
import alice.tuprolog.Prolog;
import alice.tuprolog.SolveInfo;
import alice.tuprolog.Term;
import alice.tuprolog.Theory;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    private EditText avvisi1;
    private EditText avvisi2;
    private List<String> lista;
    private MyTask myTask = new MyTask();
    private Prolog engine;
    private List<Term> clientAssertions;
    private int count;


    class MyTask extends AsyncTask<Request, Void, String> {
        private Exception e = null;

        @Override
        protected String doInBackground(Request... requests) {
            try {
                Request req = requests[0];
                OkHttpClient client = new OkHttpClient();
                Response response = client.newCall(req).execute();
                return response.body().string();
            } catch (IOException e) {
                this.e = e;
                return null;
            }
        }

        @Override
        protected void onPostExecute(String json) {
            if (this.e != null) {
                e.printStackTrace();
                MainActivity.this.avvisi1.setText(e.getMessage());
            }
            else {
                Gson gson = new Gson();
                List<String> results = gson.fromJson(json, List.class);
                for(String el : results) {
                    MainActivity.this.lista.add(el);
                }
                MainActivity.this.count++;
                if (MainActivity.this.count == 6) {
                    for (Term assertion : MainActivity.this.clientAssertions) {
                        Term retract = Term.createTerm("retract( ("+assertion.toString()+") )");
                        MainActivity.this.engine.solve(retract);
                    }
                    MainActivity.this.clientAssertions = new ArrayList<Term>();
                    for(String info : lista) {
                        if (isSuccess(info))
                        {
                            Term sol = getSolution(info);
                            MainActivity.this.clientAssertions.add(sol);
                        }
                    }
                    for (Term t : MainActivity.this.clientAssertions) {
                        Term assertion = Term.createTerm("assert( ("+t.toString()+") )");
                        MainActivity.this.engine.solve(assertion);
                    }

                    List<SolveInfo> avvertenzeEz = null;
                    List<SolveInfo> avvertenze2 = null;
                    try {
                        avvertenzeEz = solveAll(Term.createTerm("avvertenza1(X)"), MainActivity.this.engine);
                        avvertenze2 = solveAll(Term.createTerm("avvertenza2(X)"), MainActivity.this.engine);
                    } catch (NoMoreSolutionException e1) {
                        e1.printStackTrace();
                    }

                    MainActivity.this.avvisi1.setText(avvertenzeEz.toString());
                    MainActivity.this.avvisi2.setText(avvertenze2.toString());

                    MainActivity.this.count = 0;

                }

            }
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        Button but = (Button) findViewById(R.id.scaricaAvvisiButton);
        this.avvisi1 = (EditText) findViewById(R.id.textAreaAvvisi1);
        this.avvisi2 = (EditText) findViewById(R.id.textAreaAvvisi2);
        MainActivity.this.count = 0;

        this.lista = new ArrayList<String>();
        MainActivity.this.engine = new Prolog();
        clientAssertions = new ArrayList<Term>();

        Theory t;
        try {
            t = new Theory(getTheory());
            MainActivity.this.engine.setTheory(t);

        } catch (InvalidTheoryException e) {
            e.printStackTrace();
        }



        but.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                MainActivity.this.lista = new ArrayList<String>();
                MainActivity.this.count = 0;

                Request reqMalattieWaterLocal = getRequest("malattia(X).", "http://192.168.1.111:8080/rest/tuProlog/main/solutions");
                Request reqSintomiWaterLocal = getRequest("sintomo(X).", "http://192.168.1.111:8080/rest/tuProlog/main/solutions");
                Request reqAvvisiSpazzAcer = getRequest("avvertenza(X).", "http://192.168.1.114:8080/rest/tuProlog/main/solutions");
                Request reqSintomiSpazzAcer = getRequest("sintomo(X).", "http://192.168.1.114:8080/rest/tuProlog/main/solutions");
                Request reqMalattiePersonalRasp = getRequest("malattia(X).", "http://192.168.1.101:8080/rest/tuProlog/main/solutions");
                Request reqSintomiPersonalRasp = getRequest("sintomo(X).", "http://192.168.1.101:8080/rest/tuProlog/main/solutions");

                new MyTask().execute(reqMalattieWaterLocal);
                new MyTask().execute(reqSintomiWaterLocal);
                new MyTask().execute(reqAvvisiSpazzAcer);
                new MyTask().execute(reqSintomiSpazzAcer);
                new MyTask().execute(reqMalattiePersonalRasp);
                new MyTask().execute(reqSintomiPersonalRasp);



            }
        });

    }

    private static List<SolveInfo> solveAll(Term toSolve, Prolog engine) throws NoMoreSolutionException {
        List<SolveInfo> lista = new ArrayList<SolveInfo>();
        lista.add(engine.solve(toSolve));
        while (engine.hasOpenAlternatives())
        {
            lista.add(engine.solveNext());
        }
        return lista;
    }

    private void updateUI(String text) {
        avvisi1.setText(text);
    }

    private Request getRequest(String goal, String url) {
        RequestBody body = RequestBody.create(MediaType.parse("text/plain; charset=utf-8"), goal);
        Request request = new Request.Builder().url(url).post(body).build();
        return request;
    }

    private static boolean isSuccess(String solveinfoString) {
        return solveinfoString.startsWith("yes.");
    }

    private static Term getSolution(String solveinfoString) {
        String[] lines = solveinfoString.split("\n");
        String[] linesSol = lines[1].split(" / ");

        return Term.createTerm(linesSol[1]);
    }

    private String getTheory() {
        StringBuilder sb = new StringBuilder();
        sb.append("avvertenza1(assumere_meno_sodio) :- sodiuria_elevata, pressione_alta.\n");
        sb.append("avvertenza1(bere_piu_acqua) :- disidratazione.\n");
        sb.append("avvertenza1(pressione_alta) :- pressione_alta.\n");
        sb.append("avvertenza1(pressione_bassa) :- pressione_bassa.\n");
        sb.append("avvertenza1(lavare_denti) :- lavare_denti.\n");
        sb.append("avvertenza1(ricarica_batteria_spazzolino) :- ricarica_batteria.\n");
        sb.append("avvertenza2(infezione_orale) :- febbre, infezione.\n");
        sb.append("avvertenza2(infezione_urinaria) :- febbre, globuli.\n");
        sb.append("avvertenza2(screening_colon) :- sangue.\n");
        sb.append("avvertenza2(possibile_pre_diabete) :- pre_diabete.\n");
        sb.append("avvertenza2(possibile_diabete) :- diabete.\n");

        return sb.toString();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}

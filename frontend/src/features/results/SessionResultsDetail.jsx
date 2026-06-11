import Card from '../../components/ui/Card';

import ResultsCharts from './ResultsCharts';

import TrialLog from './TrialLog';



export default function SessionResultsDetail({ result }) {

  if (!result) {

    return null;

  }



  return (

    <>

      <div className="dashboard-grid results-metrics">

        <Card title="Average Reaction Time" accent="cool">

          <p className="metric-value">{result.avgReactionTime} ms</p>

        </Card>

        <Card title="Accuracy" accent="warm">

          <p className="metric-value">{result.accuracy}%</p>

        </Card>

        <Card title="Error Rate">

          <p className="metric-value">{result.errorRate}%</p>

        </Card>

        {result.falseAlarmRate != null ? (

          <Card title="False Alarm Rate">

            <p className="metric-value">{Math.round(result.falseAlarmRate * 1000) / 10}%</p>

          </Card>

        ) : null}

        {result.maxNReached != null ? (

          <Card title="Max N Reached">

            <p className="metric-value">{result.maxNReached}</p>

          </Card>

        ) : null}

        {result.maxSpanReached != null ? (

          <Card title="Max Span Reached">

            <p className="metric-value">{result.maxSpanReached}</p>

          </Card>

        ) : null}

        {result.stroopInterferenceMs != null ? (

          <Card title="Stroop Interference">

            <p className="metric-value">{result.stroopInterferenceMs} ms</p>

          </Card>

        ) : null}

        {result.stroopCongruentAccuracy != null ? (

          <Card title="Congruent Accuracy">

            <p className="metric-value">{result.stroopCongruentAccuracy}%</p>

          </Card>

        ) : null}

        {result.stroopIncongruentAccuracy != null ? (

          <Card title="Incongruent Accuracy">

            <p className="metric-value">{result.stroopIncongruentAccuracy}%</p>

          </Card>

        ) : null}

        {result.dPrime != null ? (

          <Card title="d-prime">

            <p className="metric-value">{result.dPrime}</p>

          </Card>

        ) : null}

      </div>



      <ResultsCharts result={result} />



      <Card title="Trial Log">

        <TrialLog trials={result.trials} />

      </Card>

    </>

  );

}



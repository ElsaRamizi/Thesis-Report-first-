import { useCallback, useEffect, useState } from 'react';

/**
 * Simple data-fetch hook used across pages (typical 3rd-year SPA pattern).
 */
export default function useAsyncData(fetcher, deps = []) {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const reload = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      const result = await fetcher();
      setData(result);
    } catch (requestError) {
      setError(requestError.response?.data?.message ?? 'Data could not be loaded.');
    } finally {
      setLoading(false);
    }
  }, deps);

  useEffect(() => {
    let active = true;

    (async () => {
      setLoading(true);
      setError('');
      try {
        const result = await fetcher();
        if (active) {
          setData(result);
        }
      } catch (requestError) {
        if (active) {
          setError(requestError.response?.data?.message ?? 'Data could not be loaded.');
        }
      } finally {
        if (active) {
          setLoading(false);
        }
      }
    })();

    return () => {
      active = false;
    };
  }, deps);

  return { data, loading, error, setData, setError, reload };
}

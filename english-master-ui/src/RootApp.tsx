import "primeicons/primeicons.css";

import { Suspense } from "react";
import { BrowserRouter } from "react-router-dom";
import { Provider } from "react-redux";
import store from "@/redux/store";
import PageLoader from "@/components/PageLoader";
import { PrimeReactProvider } from "primereact/api";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { AuthProvider, useAuth } from "@/context/AuthProvider";
import AppRouter from "@/router/AppRouter";
import AuthRouter from "@/router/AuthRouter";

const queryClient = new QueryClient();

function Main() {
  const { token } = useAuth();
  return token ? <AppRouter /> : <AuthRouter />;
}

export default function RouteApp() {
  return (
    <BrowserRouter>
      <QueryClientProvider client={queryClient}>
        <Provider store={store}>
          <PrimeReactProvider>
            <AuthProvider>
              <Suspense fallback={<PageLoader />}>
                <Main />
              </Suspense>
            </AuthProvider>
          </PrimeReactProvider>
        </Provider>
      </QueryClientProvider>
    </BrowserRouter>
  );
}

/* eslint-disable react-refresh/only-export-components */
import { lazy } from "react";
import { RouteObject } from "react-router-dom";

const NotFound = lazy(() => import("@/pages/NotFound"));
const Dashboard = lazy(() => import("@/pages/Dashboard"));
const Vocabulary = lazy(() => import("@/pages/Vocabulary"));
const Practice = lazy(() => import("@/pages/Practice"));
const Account = lazy(() => import("@/pages/Account"));

const routes: Record<string, RouteObject[]> = {
  default: [
    {
      path: "/",
      element: <Dashboard />,
    },
    {
      path: "/vocabulary",
      element: <Vocabulary />,
    },
    {
      path: "/practice",
      element: <Practice />,
    },
    {
      path: "/account",
      element: <Account />,
    },
    {
      path: "*",
      element: <NotFound />,
    },
  ],
};

export default routes;

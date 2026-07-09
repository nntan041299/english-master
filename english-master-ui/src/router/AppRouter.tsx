import { useRoutes, RouteObject } from 'react-router-dom';

import routes from './routes';

export default function AppRouter() {
  const routesList: RouteObject[] = [];

  Object.entries(routes).forEach(([, value]) => {
    routesList.push(...value);
  });

  return useRoutes(routesList);
}

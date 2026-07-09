import { request, ENDPOINT } from '@/rest';

export const getUserInfo = async () => {
  return request.get({ path: ENDPOINT.USERS });
};

import { USER_INFO_RETRIEVE_SUCCESS } from './types';
import { UserState } from './reducer';

export const setUserInfo = (payload: UserState) => ({
  type: USER_INFO_RETRIEVE_SUCCESS,
  payload,
});

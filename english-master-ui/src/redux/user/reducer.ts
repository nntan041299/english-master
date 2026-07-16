import { UnknownAction } from "@reduxjs/toolkit";
import * as actionTypes from "./types";

export interface UserState {
  id: string | undefined;
  username: string | undefined;
  email: string | undefined;
  firstName: string | undefined;
  lastName: string | undefined;
  avatarUrl: string | undefined;
}

interface UserInfoRetrieveSuccessAction {
  type: typeof actionTypes.USER_INFO_RETRIEVE_SUCCESS;
  payload: UserState;
}

type UserAction = UserInfoRetrieveSuccessAction;

const INITIAL_STATE: UserState = {
  id: undefined,
  username: undefined,
  email: undefined,
  firstName: undefined,
  lastName: undefined,
  avatarUrl: undefined,
};

const userReducer = (
  state: UserState = INITIAL_STATE,
  action: UserAction | UnknownAction,
): UserState => {
  switch (action.type) {
    case actionTypes.USER_INFO_RETRIEVE_SUCCESS: {
      const { payload } = action as UserInfoRetrieveSuccessAction;
      return {
        id: payload.id,
        username: payload.username,
        email: payload.email,
        firstName: payload.firstName,
        lastName: payload.lastName,
        avatarUrl: payload.avatarUrl,
      };
    }
    default:
      return state;
  }
};

export default userReducer;

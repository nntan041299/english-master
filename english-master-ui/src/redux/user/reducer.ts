import * as actionTypes from "./types";

export interface UserState {
  id: string | undefined;
  username: string | undefined;
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
  firstName: undefined,
  lastName: undefined,
  avatarUrl: undefined,
};

const userReducer = (
  state: UserState = INITIAL_STATE,
  action: UserAction,
): UserState => {
  switch (action.type) {
    case actionTypes.USER_INFO_RETRIEVE_SUCCESS:
      return {
        id: action.payload.id,
        username: action.payload.username,
        firstName: action.payload.firstName,
        lastName: action.payload.lastName,
        avatarUrl: action.payload.avatarUrl,
      };
    default:
      return state;
  }
};

export default userReducer;

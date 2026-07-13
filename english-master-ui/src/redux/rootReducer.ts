import { combineReducers, UnknownAction } from "@reduxjs/toolkit";

import { reducer as userReducer } from "./user";

export const RESET_STORE = "RESET_STORE";

const combinedReducer = combineReducers({
  user: userReducer,
});

type RootState = ReturnType<typeof combinedReducer>;

const rootReducer = (state: RootState | undefined, action: UnknownAction) => {
  if (action.type === RESET_STORE)
    return combinedReducer(undefined, action as UnknownAction);
  return combinedReducer(state, action);
};

export default rootReducer;

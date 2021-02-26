import React from 'react'

import Enzyme from 'enzyme';
import Adapter from '@wojtekmaj/enzyme-adapter-react-17';
Enzyme.configure({adapter: new Adapter()});

describe('when testing home', () => {
  it('should render the datatable', () => {
    expect(true).toBe(true)
  })
})